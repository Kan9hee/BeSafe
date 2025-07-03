package Bright.BeSafeProject.config;

import Bright.BeSafeProject.component.JwtComponent;
import Bright.BeSafeProject.component.OAuth2SuccessComponent;
import Bright.BeSafeProject.vo.SecuredPathEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtConfig jwtConfig;
    private final AuthorizationValueConfig authValueConfig;
    private final JwtComponent jwtComponent;
    private final OAuth2SuccessComponent oAuth2SuccessComponent;
    private final ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity serverHttpSecurity){
        serverHttpSecurity
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(SecuredPathEnum.allPaths().toArray(new String[0])).authenticated()
                        .pathMatchers(authValueConfig.getPermitAllUrl()).permitAll()
                )
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(
                                new RedirectServerAuthenticationEntryPoint(jwtConfig.getPath())
                        )
                )
                .oauth2Login(oauth2 -> oauth2.authenticationSuccessHandler(oAuth2SuccessComponent))
                .addFilterBefore(jwtComponent, SecurityWebFiltersOrder.AUTHENTICATION);

        return serverHttpSecurity.build();
    }

    private ServerOAuth2AuthorizationRequestResolver authorizationRequestResolver(){
        ServerWebExchangeMatcher requestMatcher =
                new PathPatternParserServerWebExchangeMatcher(authValueConfig.getOauth2().getUriPattern());

        return new DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository,requestMatcher);
    }
}
