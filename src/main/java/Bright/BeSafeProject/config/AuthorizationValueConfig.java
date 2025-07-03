package Bright.BeSafeProject.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class AuthorizationValueConfig {
    private String permitAllUrl;
    private String header;
    private Oauth2Properties oauth2;

    @Getter
    @Setter
    public static class Oauth2Properties{
        private String popupClose;
        private String uriPattern;
        private DefaultOauth2UserProperties defaultOAuth2User;

        @Getter
        @Setter
        public static class DefaultOauth2UserProperties{
            private String id;
            private String name;
            private String email;
            private String platform;
        }
    }
}
