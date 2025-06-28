package Bright.BeSafeProject.dto;

import org.springframework.http.ResponseCookie;

public record JwtDTO(
        String accessTokenString,
        String refreshTokenString,
        ResponseCookie accessTokenCookie,
        ResponseCookie refreshTokenCookie
) { }
