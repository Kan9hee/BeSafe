package Bright.BeSafeProject.component;

import Bright.BeSafeProject.config.AuthorizationValueConfig;
import Bright.BeSafeProject.dto.JwtDTO;
import Bright.BeSafeProject.service.AccountService;
import Bright.BeSafeProject.vo.AccountRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static j2html.TagCreator.*;

@Component(value = "authenticationSuccessHandler")
@RequiredArgsConstructor
public class OAuth2SuccessComponent implements ServerAuthenticationSuccessHandler {
    private final AuthorizationValueConfig authValueConfig;
    private final JwtComponent jwtComponent;
    private final AccountService accountService;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
                                              Authentication authentication) {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String platform = (String) oauth2User.getAttributes()
                .get(authValueConfig.getOauth2().getDefaultOAuth2User().getPlatform());
        String name = (String) oauth2User.getAttributes()
                .get(authValueConfig.getOauth2().getDefaultOAuth2User().getName());
        String email = (String) oauth2User.getAttributes()
                .get(authValueConfig.getOauth2().getDefaultOAuth2User().getEmail());

        return accountService.isExistsAccount(email)
                .flatMap(exist -> {
                    Mono<Void> process;
                    if(!exist){
                        process = accountService.join(
                                platform,
                                name,
                                email,
                                null,
                                AccountRoleEnum.ROLE_USER.name());
                    } else {
                        process = Mono.empty();
                    }

                    return process.then(Mono.defer(() ->{
                        ServerHttpResponse response = webFilterExchange.getExchange().getResponse();

                        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(AccountRoleEnum.ROLE_USER.name()));
                        Authentication newAuth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                        JwtDTO jwt = jwtComponent.generateToken(newAuth);

                        response.addCookie(jwt.accessTokenCookie());
                        response.addCookie(jwt.refreshTokenCookie());

                        response.getHeaders().setContentType(MediaType.TEXT_HTML);
                        String html = html(
                                body(
                                        script(rawHtml(authValueConfig.getOauth2().getPopupClose()))
                                )
                        ).render();

                        DataBuffer buffer = response.bufferFactory().wrap(html.getBytes(StandardCharsets.UTF_8));
                        return response.writeWith(Mono.just(buffer));
                    }));
                });
    }
}
