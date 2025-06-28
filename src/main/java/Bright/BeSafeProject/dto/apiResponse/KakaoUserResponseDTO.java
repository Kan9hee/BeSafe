package Bright.BeSafeProject.dto.apiResponse;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponseDTO(long id,
                                   @JsonProperty("kakao_account")
                                   KakaoAccountInfoDTO kakaoAccount) {

        public record KakaoAccountInfoDTO(
                KakaoAccountProfileDTO profile,
                String email
        ) { }

        public record KakaoAccountProfileDTO(
                String nickname
        ) { }
}
