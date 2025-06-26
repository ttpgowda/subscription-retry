package com.thewealthweb.srbackend.stripe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StripeAccessTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("livemode")
    private boolean livemode;
    @JsonProperty("refresh_token") // Usually null for standard Connect, but good to include
    private String refreshToken;
    @JsonProperty("token_type")
    private String tokenType; // "bearer"
    @JsonProperty("stripe_publishable_key")
    private String stripePublishableKey;
    @JsonProperty("stripe_user_id") // The connected account ID
    private String stripeUserId;
    @JsonProperty("scope")
    private String scope;
}