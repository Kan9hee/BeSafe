package Bright.BeSafeProject.dto.apiResponse;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoRefreshResponseDTO(
        @JsonProperty("token_type")
        String tokenType,
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("expires_in")
        int expirationTime
) { }
