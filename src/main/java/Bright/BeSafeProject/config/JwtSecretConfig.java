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
public class JwtSecretConfig {
    private String secret;

    public SecretKey getSecret(){
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(decodedKey);
    }
}
