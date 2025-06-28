package Bright.BeSafeProject.service;

import Bright.BeSafeProject.config.ApiStringConfig;
import Bright.BeSafeProject.dto.LocationDTO;
import Bright.BeSafeProject.dto.apiRequest.TmapRouteRequestDTO;
import Bright.BeSafeProject.dto.apiResponse.*;
import Bright.BeSafeProject.dto.apiResponse.StreetResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ExternalApiService {
    private final ApiStringConfig apiStringConfig;
    private final WebClient streetLightApiWebClient;
    private final WebClient kakaoAuthWebClient;
    private final WebClient kakaoApiWebClient;
    private final WebClient googleTokenApiWebClient;
    private final WebClient googleUserInfoApiWebClient;
    private final WebClient tMapRouteApiWebClient;
    private final WebClient googleRevokeTokenApiWebClient;

    public Mono<StreetResponseDTO> callStreetLightData(int pageNo, int pageSize){
        return streetLightApiWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("page",pageNo)
                        .queryParam("perPage",pageSize)
                        .queryParam("serviceKey",apiStringConfig.getOpenData().getAuthenticationKey())
                        .build())
                .retrieve()
                .bodyToMono(StreetResponseDTO.class);
    }

    public Mono<KakaoTokenResponseDTO> callKakaoAccessToken(String code){
        return kakaoAuthWebClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path(apiStringConfig.getKakao().getTokenUri())
                        .queryParam("grant_type","authorization_code")
                        .queryParam("client_id",apiStringConfig.getKakao().getClientId())
                        .queryParam("redirect_uri","http://localhost:8080/member/kakao")
                        .queryParam("code",code)
                        .build())
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .bodyToMono(KakaoTokenResponseDTO.class);
    }

    public Mono<KakaoRefreshResponseDTO> refreshKakaoAccessToken(String refreshToken){
        return kakaoAuthWebClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path(apiStringConfig.getKakao().getTokenUri())
                        .queryParam("grant_type","authorization_code")
                        .queryParam("client_id",apiStringConfig.getKakao().getClientId())
                        .queryParam("refresh_token",refreshToken)
                        .build())
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .bodyToMono(KakaoRefreshResponseDTO.class);
    }

    public Mono<KakaoUserResponseDTO> callKakaoUserInfo(String accessToken){
        return kakaoApiWebClient
                .get()
                .uri(apiStringConfig.getKakao().getUserInfoUri())
                .headers(httpHeaders ->{
                    httpHeaders.setBearerAuth(accessToken);
                    httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
                }).retrieve()
                .bodyToMono(KakaoUserResponseDTO.class);
    }

    public Mono<Long> logOutKakaoUser(String accessToken){
        return kakaoApiWebClient
                .post()
                .uri(apiStringConfig.getKakao().getLogOutUri())
                .header(HttpHeaders.AUTHORIZATION,"Bearer "+accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Object idObj = response.get("id");
                    if (idObj == null)
                        throw new IllegalStateException("id is null in Kakao log-out response");
                    if (!(idObj instanceof Number))
                        throw new IllegalStateException("id is not a number: " + idObj);
                    return ((Number) idObj).longValue();
                });
    }

    public Mono<Long> signOutKakaoUser(String accessToken){
        return kakaoApiWebClient
                .post()
                .uri(apiStringConfig.getKakao().getUnlinkUri())
                .header(HttpHeaders.AUTHORIZATION,"Bearer "+accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Object idObj = response.get("id");
                    return ((Number) idObj).longValue();
                });
    }

    public Mono<GoogleTokenResponseDTO> callGoogleUserToken(String callbackCode){
        return googleTokenApiWebClient
                .post()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData("code", callbackCode)
                        .with("client_id", apiStringConfig.getGoogle().getClientId())
                        .with("client_secret", apiStringConfig.getGoogle().getClientPassword())
                        .with("redirect_uri", "http://localhost:8080/oauth/google/callback")
                        .with("grant_type", "authorization_code"))
                .retrieve()
                .bodyToMono(GoogleTokenResponseDTO.class);
    }

    public Mono<GoogleTokenResponseDTO> refreshGoogleUserToken(String refreshToken){
        return googleTokenApiWebClient
                .post()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData("refresh_token", refreshToken)
                        .with("client_id", apiStringConfig.getGoogle().getClientId())
                        .with("client_secret", apiStringConfig.getGoogle().getClientPassword())
                        .with("redirect_uri", "http://localhost:8080/oauth/google/callback")
                        .with("grant_type", "refresh_token"))
                .retrieve()
                .bodyToMono(GoogleTokenResponseDTO.class);
    }

    public Mono<GoogleUserResponseDTO> callGoogleUserInfo(String accessToken){
        return googleUserInfoApiWebClient
                .get()
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GoogleUserResponseDTO.class);
    }

    public Mono<HttpStatusCode> revokeGoogleUserToken(String accessToken){
        return googleRevokeTokenApiWebClient
                .post()
                .headers(httpHeaders ->
                        httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                )
                .bodyValue(BodyInserters.fromFormData("token", accessToken))
                .exchangeToMono(response -> Mono.just(response.statusCode()));
    }

    public Mono<List<LocationDTO>> callTmapRoute(TmapRouteRequestDTO request){
        return tMapRouteApiWebClient
                .post()
                .header("accept","application/json")
                .header("appKey",apiStringConfig.getSk().getAppKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TmapRouteResponseDTO.class)
                .map(response -> response.features()
                        .stream()
                        .flatMap(feature -> {
                            TmapRouteResponseGeometryDto geometry = feature.geometry();
                            Object coords = geometry.coordinates();
                            if (geometry.type().equalsIgnoreCase("Point")) {
                                List<?> point = (List<?>) coords;
                                return Stream.of(new LocationDTO(
                                        ((Number) point.get(0)).doubleValue(),
                                        ((Number) point.get(1)).doubleValue()
                                ));
                            } else if (geometry.type().equalsIgnoreCase("LineString")) {
                                List<?> linePoints = (List<?>) coords;
                                return linePoints.stream()
                                        .map(obj -> {
                                            List<?> coord = (List<?>) obj;
                                            return new LocationDTO(
                                                    ((Number) coord.get(0)).doubleValue(),
                                                    ((Number) coord.get(1)).doubleValue()
                                            );
                                        });
                            }
                            return Stream.empty();
                        })
                        .toList()
                );
    }
}
