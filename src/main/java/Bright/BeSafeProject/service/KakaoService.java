package Bright.BeSafeProject.service;

import Bright.BeSafeProject.model.Member;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class KakaoService {

    @Value("${KAKAO_CLIENT_ID}")
    private String kakao_client_id;

    public String callAccessToken(String code) throws ParseException {
        String tokenJSON = WebClient.create("https://kauth.kakao.com/oauth/token")
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("grant_type","authorization_code")
                        .queryParam("client_id",kakao_client_id)
                        .queryParam("redirect_uri","http://localhost:8080/member/kakao")
                        .queryParam("code",code)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(tokenJSON);
        return (String)jsonObject.get("access_token");
    }

    public Member callUserInfo(String token) throws ParseException {
        String userInfoJSON = WebClient.builder()
                .baseUrl("https://kapi.kakao.com/v2/user/me")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build()
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(userInfoJSON);
        JSONObject account = (JSONObject) jsonObject.get("kakao_account");
        JSONObject profile = (JSONObject) account.get("profile");
        String nickname = (String) profile.get("nickname");
        String email=(String)account.get("email");
        return new Member(token,nickname,email);
    }

    public void returnAccessToken(String token){
        String returnJSON = WebClient.builder()
                .baseUrl("https://kapi.kakao.com/v1/user/logout")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build()
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .block();
        System.out.println(returnJSON);
    }
}
