package Bright.BeSafeProject.service;

import Bright.BeSafeProject.config.AuthorizationValueConfig;
import Bright.BeSafeProject.vo.AccountRoleEnum;
import Bright.BeSafeProject.vo.PlatformEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements ReactiveOAuth2UserService<OAuth2UserRequest,OAuth2User> {
    private final AuthorizationValueConfig authValueConfig;
    private final ExternalApiService externalApiService;

    @Override
    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String accessToken = userRequest.getAccessToken().getTokenValue();

        if(registrationId.equals(PlatformEnum.GOOGLE.name().toLowerCase())){
            return externalApiService.callGoogleUserInfo(accessToken)
                    .map(user -> new DefaultOAuth2User(
                            List.of(new SimpleGrantedAuthority(AccountRoleEnum.ROLE_USER.name())),
                            Map.of(
                                    authValueConfig.getOauth2().getDefaultOAuth2User().getId(),user.sub(),
                                    authValueConfig.getOauth2().getDefaultOAuth2User().getName(),user.name(),
                                    authValueConfig.getOauth2().getDefaultOAuth2User().getEmail(),user.email(),
                                    authValueConfig.getOauth2().getDefaultOAuth2User().getPlatform(), PlatformEnum.GOOGLE.name()
                            ),
                            authValueConfig.getOauth2().getDefaultOAuth2User().getId()
                    ));
        }
        else if(registrationId.equals(PlatformEnum.KAKAO.name().toLowerCase())){
            return externalApiService.callKakaoUserInfo(accessToken)
                    .map(user -> new DefaultOAuth2User(
                            List.of(new SimpleGrantedAuthority(AccountRoleEnum.ROLE_USER.name())),
                            Map.of(
                                    authValueConfig.getOauth2().getDefaultOAuth2User().getId(),user.id(),
                                    authValueConfig.getOauth2().getDefaultOAuth2User().getName(),user.kakaoAccount().profile().nickname(),
                                    authValueConfig.getOauth2().getDefaultOAuth2User().getEmail(),user.kakaoAccount().email(),
                                    authValueConfig.getOauth2().getDefaultOAuth2User().getPlatform(), PlatformEnum.KAKAO.name()
                            ),
                            authValueConfig.getOauth2().getDefaultOAuth2User().getId()
                    ));
        }

        return Mono.error(new IllegalArgumentException("지원하지 않는 플랫폼입니다."));
    }
}
