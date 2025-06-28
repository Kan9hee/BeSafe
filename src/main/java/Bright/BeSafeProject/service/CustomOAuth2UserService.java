package Bright.BeSafeProject.service;

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
    private final ExternalApiService externalApiService;

    @Override
    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String accessToken = userRequest.getAccessToken().getTokenValue();

        if(registrationId.equals("google")){
            return externalApiService.callGoogleUserInfo(accessToken)
                    .map(user -> new DefaultOAuth2User(
                            List.of(new SimpleGrantedAuthority(AccountRoleEnum.ROLE_USER.name())),
                            Map.of(
                                    "id",user.sub(),
                                    "name",user.name(),
                                    "email",user.email(),
                                    "platform", PlatformEnum.GOOGLE.name()
                            ),
                            "id"
                    ));
        }
        else if(registrationId.equals("kakao")){
            return externalApiService.callKakaoUserInfo(accessToken)
                    .map(user -> new DefaultOAuth2User(
                            List.of(new SimpleGrantedAuthority(AccountRoleEnum.ROLE_USER.name())),
                            Map.of(
                                    "id",user.id(),
                                    "name",user.kakaoAccount().profile().nickname(),
                                    "email",user.kakaoAccount().email(),
                                    "platform", PlatformEnum.KAKAO.name()
                            ),
                            "id"
                    ));
        }

        return Mono.error(new IllegalArgumentException("지원하지 않는 플랫폼입니다."));
    }
}
