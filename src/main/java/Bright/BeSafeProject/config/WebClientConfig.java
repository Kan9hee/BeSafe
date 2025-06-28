package Bright.BeSafeProject.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class WebClientConfig {
    @Autowired
    private ApiStringConfig apiStringConfig;

    @Bean
    public WebClient streetLightApiWebClient(){
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(
                apiStringConfig.getOpenData().getStreetLightsUrl()
        );
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        return WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(apiStringConfig.getOpenData().getStreetLightsUrl())
                .codecs(config -> config.defaultCodecs().maxInMemorySize(5*1024*1024))
                .build();
    }

    @Bean
    public WebClient tMapApiWebClient(){
        return WebClient.builder()
                .baseUrl(apiStringConfig.getSk().getTMap())
                .build();
    }

    @Bean
    public WebClient kakaoAuthWebClient(){
        return WebClient.builder()
                .baseUrl(apiStringConfig.getKakao().getAuthBaseUrl())
                .build();
    }

    @Bean
    public WebClient kakaoApiWebClient(){
        return WebClient.builder()
                .baseUrl(apiStringConfig.getKakao().getApiBaseUrl())
                .build();
    }

    @Bean
    public WebClient googleTokenApiWebClient(){
        return WebClient.builder()
                .baseUrl(apiStringConfig.getGoogle().getAccessTokenUrl())
                .build();
    }

    @Bean
    public WebClient googleUserInfoApiWebClient(){
        return WebClient.builder()
                .baseUrl(apiStringConfig.getGoogle().getUserInfoUrl())
                .build();
    }

    @Bean
    public WebClient googleRevokeTokenApiWebClient(){
        return WebClient.builder()
                .baseUrl(apiStringConfig.getGoogle().getRevokeTokenUrl())
                .build();
    }

    @Bean
    public WebClient tMapRouteApiWebClient(){
        return WebClient.builder()
                .baseUrl(apiStringConfig.getSk().getTMap())
                .build();
    }
}
