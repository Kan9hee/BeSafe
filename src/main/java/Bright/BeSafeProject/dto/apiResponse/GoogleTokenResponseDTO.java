package Bright.BeSafeProject.dto.apiResponse;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponseDTO(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("refresh_token")
        String refreshToken
) { }
