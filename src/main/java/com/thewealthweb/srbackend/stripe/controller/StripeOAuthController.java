package com.thewealthweb.srbackend.stripe.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.thewealthweb.srbackend.common.dto.ErrorMessage;
import com.thewealthweb.srbackend.stripe.dto.StripeAccessTokenResponse;
import com.thewealthweb.srbackend.stripe.service.StripeOAuthService;
import com.thewealthweb.srbackend.tenant.config.TenantContext;
import com.thewealthweb.srbackend.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@RestController
@RequestMapping("/stripe/oauth")
@RequiredArgsConstructor
@Slf4j
public class StripeOAuthController {

    private final StripeOAuthService stripeOAuthService;

    /**
     * Endpoint to initiate the Stripe Connect OAuth flow.
     * Authenticated users of your platform (e.g., Owners) will call this.
     * @param currentUser The currently authenticated user (Spring Security principal).
     * @return A RedirectView to send the user to Stripe's authorization page.
     */
    @GetMapping("/connect")
    public RedirectView connectStripeAccount(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null || currentUser.getTenant() == null || currentUser.getTenant().getTenantId() == null) {
            log.warn("Unauthorized attempt to connect Stripe account - no current user, tenant, or tenantId.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication and valid tenant context required to connect Stripe.");
        }

        String logicalTenantId = currentUser.getTenant().getTenantId(); // Get the String tenantId from the Tenant object
        Long userId = currentUser.getId();

        String stripeOAuthUrl = stripeOAuthService.generateOAuthUrl(logicalTenantId, userId); // Pass the String tenantId
        log.info("Redirecting tenant {} to Stripe for OAuth.", logicalTenantId);
        return new RedirectView(stripeOAuthUrl);
    }

    // ... (rest of the controller methods remain largely the same, they rely on TenantContext) ...

    @GetMapping("/data/subscriptions")
    public ResponseEntity<?> getStripeSubscriptions(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return new ResponseEntity<>(new ErrorMessage(403,"Unauthorized", "User not authenticated"), HttpStatus.UNAUTHORIZED);
        }
        // Ensure tenant context is set for the current user's tenant
        TenantContext.setTenantId(currentUser.getTenant().getTenantId()); // Set the String logicalTenantId

        try {
            List<Subscription> subscriptions = stripeOAuthService.fetchAllSubscriptions();
            return ResponseEntity.ok(subscriptions);
        } catch (StripeException e) {
            log.error("Error fetching subscriptions for tenant {}: {}", TenantContext.getTenantId(), e.getMessage());
            return new ResponseEntity<>(new ErrorMessage(1, "Stripe API Error", e.getMessage()), HttpStatus.BAD_GATEWAY);
        } finally {
            TenantContext.clear();
        }
    }
// In StripeOAuthController.java, inside stripeOAuthCallback method

    @GetMapping("/callback")
    public RedirectView stripeOAuthCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @RequestParam(value = "error", required = false) String error, // <-- Error parameter
            @RequestParam(value = "error_description", required = false) String errorDescription) { // <-- Error description

        if (error != null) {
            log.warn("Stripe OAuth error received: {}. Description: {}", error, errorDescription);
            // Redirect to an error page in your application
            // You might want to pass error details to the frontend
            return new RedirectView("/stripe/connect/error?message=" + errorDescription);
        }

        // In StripeOAuthController.java, inside stripeOAuthCallback method, within the try block
        try {
            StripeAccessTokenResponse tokenResponse = stripeOAuthService.exchangeCodeForAccessToken(code, state);

            log.info("Stripe account connected successfully for logicalTenantId: {}", TenantContext.getTenantId());
            // Redirect to a success page in your application
            return new RedirectView("/stripe/connect/success"); // Example success page URL

        } catch (ResponseStatusException e) {
            log.error("Stripe OAuth callback failed with HTTP status {}: {}", e.getStatusCode(), e.getReason());
            // Redirect to a specific error page with more details if possible
            return new RedirectView("/stripe/connect/error?message=" + e.getReason());
        } catch (Exception e) {
            log.error("Unexpected error during Stripe OAuth callback: {}", e.getMessage(), e);
            // Generic error page
            return new RedirectView("/stripe/connect/error?message=An unexpected error occurred.");
        } finally {
            TenantContext.clear(); // Ensure context is cleared
        }
    }
}