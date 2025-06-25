package com.thewealthweb.srbackend.stripe.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.net.RequestOptions;
import com.thewealthweb.srbackend.stripe.dto.StripeAccessTokenResponse;
import com.thewealthweb.srbackend.stripe.entity.StripeAccountConnection;
import com.thewealthweb.srbackend.stripe.repository.StripeAccountConnectionRepository;
import com.thewealthweb.srbackend.tenant.config.TenantContext; // This should hold the String tenantId
import com.thewealthweb.srbackend.tenant.entity.Tenant;
import com.thewealthweb.srbackend.tenant.repository.TenantRepository; // You will need this
import org.jasypt.encryption.StringEncryptor; // Import Jasypt's StringEncryptor
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono; // Import for WebClient error handling

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeOAuthService {

    @Value("${stripe.connect.client-id}")
    private String clientId;

    @Value("${stripe.connect.client-secret}")
    private String clientSecret;

    @Value("${stripe.connect.redirect-uri}")
    private String redirectUri;

    @Value("${stripe.connect.scope}")
    private String scope;

    private final WebClient.Builder webClientBuilder;
    private final StripeAccountConnectionRepository stripeAccountConnectionRepository;
    private final TenantRepository tenantRepository; // Inject TenantRepository to fetch Tenant entity by its String tenantId
    private final StringEncryptor jasyptStringEncryptor; // <--- INJECT JASYPT ENCRYPTOR

    // ... (encryption/decryption methods remain the same) ...

    /**
     * Generates the Stripe Connect OAuth URL with a unique state parameter.
     * The state parameter is crucial for CSRF protection and to link the callback
     * to the originating tenant/user.
     * @param logicalTenantId The String ID of the current tenant (e.g., "acme-corp")
     * @param userId The ID of the current user
     * @return The full Stripe OAuth URL to redirect the user to.
     */
    @Transactional
    public String generateOAuthUrl(String logicalTenantId, Long userId) {
        // Generate a unique state parameter. You should store this state
        // along with the logicalTenantId/userId in your database or a secure cache
        // to validate upon callback.
        String uniqueState = UUID.randomUUID().toString() + "-" + logicalTenantId + "-" + userId;
        // In a real app, store uniqueState in a database table like `oauth_states`
        // linked to logicalTenantId and userId, with an expiry time.

        return UriComponentsBuilder.fromHttpUrl("https://connect.stripe.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("scope", scope)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", uniqueState)
                .toUriString();
    }

    /**
     * Exchanges the authorization code received from Stripe for an access token.
     * This is a server-to-server call.
     * @param code The authorization code from Stripe's redirect.
     * @param state The state parameter from Stripe's redirect.
     * @return StripeAccessTokenResponse containing access token and connected account ID.
     * @throws ResponseStatusException if the exchange fails or state is invalid.
     */
    @Transactional
    public StripeAccessTokenResponse exchangeCodeForAccessToken(String code, String state) {
        // 1. Validate the 'state' parameter for CSRF protection and tenant identification
        String[] stateParts = state.split("-");
        if (stateParts.length != 3) {
            log.error("Invalid state parameter format: {}", state);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state parameter.");
        }
        String storedUuid = stateParts[0];
        String logicalTenantId = stateParts[1]; // Extract the String tenantId
        Long userId = Long.parseLong(stateParts[2]);

        // IMPORTANT: In a real application, you would check if this `storedUuid`
        // exists in your `oauth_states` table/cache and is valid for the `logicalTenantId` and `userId`
        // and hasn't expired. If valid, you'd then delete it to prevent replay attacks.

        // Set tenant context for this operation if not already set by a filter
        TenantContext.setTenantId(logicalTenantId); // Set the String logicalTenantId
        log.info("Processing Stripe OAuth callback for logicalTenantId: {}", logicalTenantId);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("code", code);

        try {
            StripeAccessTokenResponse response = webClientBuilder.build()
                    .post()
                    .uri("https://connect.stripe.com/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("Stripe OAuth token exchange failed for logicalTenantId {}: Status {} - Body: {}", logicalTenantId, clientResponse.statusCode(), errorBody);
                                        // Explicitly specify the generic type of Mono.error()
                                        return Mono.<ResponseStatusException>error(
                                                new ResponseStatusException(clientResponse.statusCode(), "Stripe OAuth token exchange failed: " + errorBody)
                                        );
                                    }))
                    .bodyToMono(StripeAccessTokenResponse.class)
                    .block();

            if (response == null || response.getAccessToken() == null || response.getStripeUserId() == null) {
                log.error("Stripe OAuth response was incomplete for logicalTenantId: {}", logicalTenantId);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Stripe OAuth token exchange failed: Incomplete response.");
            }

            // Save the connected account details
            saveStripeAccountConnection(logicalTenantId, response); // Pass the String logicalTenantId

            return response;

        } catch (Exception e) {
            log.error("Error during Stripe OAuth token exchange for logicalTenantId {}: {}", logicalTenantId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to connect to Stripe: " + e.getMessage(), e);
        } finally {
            TenantContext.clear(); // Clear tenant context
        }
    }

    @Transactional
    public void saveStripeAccountConnection(String logicalTenantId, StripeAccessTokenResponse tokenResponse) {
        // Fetch the Tenant entity using its *logical* tenantId (String)
        Tenant currentTenant = tenantRepository.findByTenantId(logicalTenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found with logical ID: " + logicalTenantId));

        StripeAccountConnection connection = stripeAccountConnectionRepository.findByTenant(currentTenant)
                .orElse(new StripeAccountConnection());

        connection.setTenant(currentTenant); // Set the Tenant entity here
        connection.setStripeUserId(tokenResponse.getStripeUserId());
        connection.setAccessToken(encryptToken(tokenResponse.getAccessToken())); // Encrypt the token!
        connection.setStripePublishableKey(tokenResponse.getStripePublishableKey());

        stripeAccountConnectionRepository.save(connection);
        log.info("Stripe account connection saved successfully for logical tenant ID: {}", logicalTenantId);
    }

    /**
     * Retrieves the encrypted access token for the current tenant.
     * This method assumes the TenantContext is already set.
     * @return Decrypted Stripe access token.
     * @throws ResponseStatusException if no connection found or error decrypting.
     */
    public String getStripeAccessTokenForCurrentTenant() {
        String logicalTenantId = TenantContext.getTenantId();
        if (logicalTenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant context not set.");
        }
        Tenant currentTenant = tenantRepository.findByTenantId(logicalTenantId) // Find by logicalTenantId
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found with logical ID: " + logicalTenantId));


        StripeAccountConnection connection = stripeAccountConnectionRepository.findByTenant(currentTenant)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stripe account not connected for this tenant."));

        return decryptToken(connection.getAccessToken());
    }

    /**
     * Retrieves the connected Stripe user ID for the current tenant.
     * This method assumes the TenantContext is already set.
     * @return Connected Stripe user ID.
     * @throws ResponseStatusException if no connection found.
     */
    public String getStripeUserIdForCurrentTenant() {
        String logicalTenantId = TenantContext.getTenantId();
        if (logicalTenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant context not set.");
        }
        Tenant currentTenant = tenantRepository.findByTenantId(logicalTenantId) // Find by logicalTenantId
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found with logical ID: " + logicalTenantId));

        StripeAccountConnection connection = stripeAccountConnectionRepository.findByTenant(currentTenant)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stripe account not connected for this tenant."));

        return connection.getStripeUserId();
    }

    /**
     * Fetches all subscriptions for a connected Stripe account.
     * @return List of Stripe Subscription objects.
     * @throws StripeException if Stripe API call fails.
     */
    public List<Subscription> fetchAllSubscriptions() throws StripeException {
        String accessToken = getStripeAccessTokenForCurrentTenant();
        String stripeUserId = getStripeUserIdForCurrentTenant();
        RequestOptions options = buildRequestOptionsForConnectedAccount(accessToken, stripeUserId);

        Map<String, Object> params = new HashMap<>();
        params.put("limit", 100); // Fetch up to 100 per page
        params.put("status", "all"); // Fetch all statuses (active, past_due, canceled, etc.)

        List<Subscription> subscriptions = new java.util.ArrayList<>();
        SubscriptionCollection subscriptionCollection;
        String startingAfter = null;

        do {
            if (startingAfter != null) {
                params.put("starting_after", startingAfter);
            }

            subscriptionCollection = Subscription.list(params, options);
            subscriptions.addAll(subscriptionCollection.getData());

            // Check if there are more items to retrieve
            if (subscriptionCollection.getData().size() == (Integer) params.get("limit")) {
                startingAfter = subscriptionCollection.getData().get(subscriptionCollection.getData().size() - 1).getId();
            } else {
                startingAfter = null; // No more pages
            }
        } while (startingAfter != null);

        log.info("Fetched {} subscriptions for Stripe account {}", subscriptions.size(), stripeUserId);
        return subscriptions;
    }

    /**
     * Constructs RequestOptions for Stripe API calls to a connected account.
     * This is essential for all API interactions on behalf of your client.
     * @param accessToken The access token for the connected account.
     * @param stripeUserId The connected Stripe account ID.
     * @return RequestOptions configured for the connected account.
     */
    private RequestOptions buildRequestOptionsForConnectedAccount(String accessToken, String stripeUserId) {
        return RequestOptions.builder()
                .setApiKey(accessToken) // Use the connected account's access token
                .setStripeAccount(stripeUserId) // Essential: Acts on behalf of this connected account
                .build();
    }

    /**
     * Encrypts the Stripe access token using Jasypt.
     * @param token The plain access token.
     * @return The encrypted access token.
     */
    private String encryptToken(String token) {
        if (token == null) {
            return null;
        }
        return jasyptStringEncryptor.encrypt(token);
    }

    /**
     * Decrypts the Stripe access token using Jasypt.
     * @param encryptedToken The encrypted access token.
     * @return The plain access token.
     */
    private String decryptToken(String encryptedToken) {
        if (encryptedToken == null) {
            return null;
        }
        return jasyptStringEncryptor.decrypt(encryptedToken);
    }
}