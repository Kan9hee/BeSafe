package Bright.BeSafeProject.dto.apiResponse;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoTokenResponseDTO(
        @JsonProperty("token_type")
        String tokenType,
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("expires_in")
        int expirationTime,
        @JsonProperty("refresh_token")
        String refreshToken,
        @JsonProperty("refresh_token_expires_in")
        int refreshTokenExpirationTime
) { }
