package Bright.BeSafeProject.service;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class KakaoLoginService {

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

        System.out.println(tokenJSON);

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(tokenJSON);
        return (String)jsonObject.get("access_token");
    }

    public void callUserInfo(String token){
        String userInfoJSON = WebClient.builder()
                .baseUrl("https://kapi.kakao.com/v2/user/me")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build()
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .block();
        System.out.println(userInfoJSON);
    }
}
