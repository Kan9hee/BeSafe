package Bright.BeSafeProject.service;

import Bright.BeSafeProject.config.ApiParamConfig;
import Bright.BeSafeProject.config.ApiStringConfig;
import Bright.BeSafeProject.config.AuthorizationValueConfig;
import Bright.BeSafeProject.dto.LocationDTO;
import Bright.BeSafeProject.dto.apiRequest.TmapRouteRequestDTO;
import Bright.BeSafeProject.dto.apiResponse.*;
import Bright.BeSafeProject.dto.apiResponse.StreetResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ExternalApiService {
    private final AuthorizationValueConfig authValueConfig;
    private final ApiStringConfig apiStringConfig;
    private final ApiParamConfig apiParamConfig;
    private final WebClient streetLightApiWebClient;
    private final WebClient kakaoApiWebClient;
    private final WebClient googleUserInfoApiWebClient;
    private final WebClient tMapRouteApiWebClient;

    public Mono<StreetResponseDTO> callStreetLightData(int pageNo, int pageSize){
        return streetLightApiWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam(apiParamConfig.getStreetLightApiCall().getPage(),pageNo)
                        .queryParam(apiParamConfig.getStreetLightApiCall().getPageSize(),pageSize)
                        .queryParam(apiParamConfig.getStreetLightApiCall().getKey(),apiStringConfig.getOpenData().getAuthenticationKey())
                        .build())
                .retrieve()
                .bodyToMono(StreetResponseDTO.class);
    }

    public Mono<KakaoUserResponseDTO> callKakaoUserInfo(String accessToken){
        return kakaoApiWebClient
                .get()
                .uri(apiStringConfig.getKakao().getUserInfoUri())
                .headers(httpHeaders -> {
                    httpHeaders.setBearerAuth(accessToken);
                    httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
                }).retrieve()
                .bodyToMono(KakaoUserResponseDTO.class);
    }

    public Mono<Long> logOutKakaoUser(String accessToken){
        return kakaoApiWebClient
                .post()
                .uri(apiStringConfig.getKakao().getLogOutUri())
                .header(HttpHeaders.AUTHORIZATION, headerValueBuilder(accessToken))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> ((Number) response.get("id")).longValue());
    }

    public Mono<Long> signOutKakaoUser(String accessToken){
        return kakaoApiWebClient
                .post()
                .uri(apiStringConfig.getKakao().getUnlinkUri())
                .header(HttpHeaders.AUTHORIZATION, headerValueBuilder(accessToken))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Object idObj = response.get("id");
                    return ((Number) idObj).longValue();
                });
    }

    public Mono<GoogleUserResponseDTO> callGoogleUserInfo(String accessToken){
        return googleUserInfoApiWebClient
                .get()
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GoogleUserResponseDTO.class);
    }

    public Mono<List<LocationDTO>> callTmapRoute(TmapRouteRequestDTO request){
        return tMapRouteApiWebClient
                .post()
                .header(apiParamConfig.getRouteApiCall().getAccept(),apiParamConfig.getRouteApiCall().getAcceptValue())
                .header(apiParamConfig.getRouteApiCall().getKey(),apiStringConfig.getSk().getAppKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TmapRouteResponseDTO.class)
                .map(response -> response.features()
                        .stream()
                        .flatMap(feature -> {
                            TmapRouteResponseGeometryDto geometry = feature.geometry();
                            Object coords = geometry.coordinates();
                            if (geometry.type().equalsIgnoreCase(apiParamConfig.getRouteApiCall().getGeoPoint())) {
                                List<?> point = (List<?>) coords;
                                return Stream.of(new LocationDTO(
                                        ((Number) point.get(0)).doubleValue(),
                                        ((Number) point.get(1)).doubleValue()
                                ));
                            } else if (geometry.type().equalsIgnoreCase(apiParamConfig.getRouteApiCall().getGeoLine())) {
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

    private String headerValueBuilder(String accessToken) { return String.join(authValueConfig.getHeader(),accessToken); }
}
