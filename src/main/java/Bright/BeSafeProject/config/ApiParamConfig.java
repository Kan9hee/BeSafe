package Bright.BeSafeProject.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "api-param")
@Getter
@Setter
public class ApiParamConfig {
    private String lightNodeLocation;
    private StreetLightApiParamProperties streetLightApiCall;
    private RouteApiParamProperties routeApiCall;

    @Getter
    @Setter
    public static class StreetLightApiParamProperties{
        private String page;
        private String pageSize;
        private String key;
    }

    @Getter
    @Setter
    public static class RouteApiParamProperties{
        private String accept;
        private String acceptValue;
        private String key;
        private String geoPoint;
        private String geoLine;
    }
}
