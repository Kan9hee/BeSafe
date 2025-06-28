package Bright.BeSafeProject.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "api-string")
@Getter
@Setter
public class ApiStringConfig {
    private OpenDataProperties openData;
    private SkProperties sk;
    private KakaoProperties kakao;
    private GoogleProperties google;

    @Getter
    @Setter
    public static class OpenDataProperties{
        private String streetLightsUrl;
        private String securityLightsUrl;
        private String authenticationKey;
    }

    @Getter
    @Setter
    public static class SkProperties{
        private String tMap;
        private String appKey;
    }

    @Getter
    @Setter
    public static class KakaoProperties{
        private String authBaseUrl;
        private String apiBaseUrl;
        private String logInUri;
        private String tokenUri;
        private String userInfoUri;
        private String logOutUri;
        private String unlinkUri;
        private String clientId;
    }

    @Getter
    @Setter
    public static class GoogleProperties{
        private String userInfoUrl;
        private String revokeTokenUrl;
        private String callbackUrl;
        private String authUrl;
        private String accessTokenUrl;
        private String profileScopeUrl;
        private String mailScopeUrl;
        private String clientId;
        private String clientPassword;
        private String apiKey;
    }
}
