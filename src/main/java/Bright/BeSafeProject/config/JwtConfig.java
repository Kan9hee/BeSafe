package Bright.BeSafeProject.config;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.util.Base64;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfig {
    private String secret;
    private String accessTokenName;
    private String refreshTokenName;
    private String path;
    private String cookieSameSite;
    private String delimiter;
    private Long accessTokenExpiration;
    private Long accessTokenCookieExpirationMinutes;
    private Long refreshTokenExpiration;
    private Long refreshTokenCookieExpirationDays;
    private Long discardTime;

    public SecretKey getSecret(){
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(decodedKey);
    }
}
