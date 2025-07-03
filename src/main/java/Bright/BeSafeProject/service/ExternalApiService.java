package Bright.BeSafeProject.service;

import Bright.BeSafeProject.config.ApiParamConfig;
import Bright.BeSafeProject.config.ApiStringConfig;
import Bright.BeSafeProject.config.AuthorizationValueConfig;
import Bright.BeSafeProject.dto.LocationDTO;
import Bright.BeSafeProject.dto.apiRequest.TmapRouteRequestDTO;
import Bright.BeSafeProject.dto.apiResponse.*;
import Bright.BeSafeProject.dto.apiResponse.StreetResponseDTO;
import Bright.BeSafeProject.exception.CustomException;
import Bright.BeSafeProject.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
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
                .onStatus(HttpStatusCode::is4xxClientError, handle4xxError())
                .onStatus(HttpStatusCode::is5xxServerError, handle5xxError())
                .bodyToMono(StreetResponseDTO.class);
    }

    public Mono<KakaoUserResponseDTO> callKakaoUserInfo(String accessToken){
        return kakaoApiWebClient
                .get()
                .uri(apiStringConfig.getKakao().getUserInfoUri())
                .headers(httpHeaders -> {
                    httpHeaders.setBearerAuth(accessToken);
                    httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
                })
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, handle4xxError())
                .onStatus(HttpStatusCode::is5xxServerError, handle5xxError())
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
                .map(response -> ((Number) response.get("id")).longValue());
    }

    public Mono<GoogleUserResponseDTO> callGoogleUserInfo(String accessToken){
        return googleUserInfoApiWebClient
                .get()
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, handle4xxError())
                .onStatus(HttpStatusCode::is5xxServerError, handle5xxError())
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
                .onStatus(HttpStatusCode::is4xxClientError, handle4xxError())
                .onStatus(HttpStatusCode::is5xxServerError, handle5xxError())
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

    private Function<ClientResponse,Mono<? extends Throwable>> handle4xxError(){
        return response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            log.error(errorBody);
                            return Mono.error(new CustomException(ErrorCode.INTERNAL_API_VALUE_ERROR));
                        });
    }

    private Function<ClientResponse,Mono<? extends Throwable>> handle5xxError(){
        return response -> response.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    log.error(errorBody);
                    return Mono.error(new CustomException(ErrorCode.INTERNAL_API_SERVER_ERROR));
                });
    }

    private String headerValueBuilder(String accessToken) { return String.join(authValueConfig.getHeader(),accessToken); }
}
