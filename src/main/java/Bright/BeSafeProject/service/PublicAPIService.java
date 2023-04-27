package Bright.BeSafeProject.service;

import Bright.BeSafeProject.model.StreetLight;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PublicAPIService {

    private Double lightLatitude;
    private Double lightLongitude;

    private ExchangeStrategies exchangeStrategies=ExchangeStrategies.builder()
            .codecs(configure->configure.defaultCodecs().maxInMemorySize(-1))
            .build();

    @Value("${PUBLIC_DATA_KEY}")
    private String public_apiKey;

    public void callSecurityLight(StreetLight streetLight,Double[] range,String address) throws IOException, ParseException {

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(getSecurityLightJSON(address));
        JSONObject x1 = (JSONObject) jsonObject.get("response");
        JSONObject x2 = (JSONObject) x1.get("body");
        JSONObject x3 = (JSONObject) x2.get("items");
        JSONArray list = (JSONArray) x3.get("item");
        for (int i = 0; i < list.size(); i++) {
            JSONObject object = (JSONObject) list.get(i);
            lightLatitude=(Double) object.get("LATITUDE");
            lightLongitude=(Double) object.get("LONGITUDE");
            if (lightLatitude >= range[0]
                    && lightLongitude >= range[1]
                    && lightLatitude <= range[2]
                    && lightLongitude <= range[3]) {
                streetLight.addLatitude(lightLatitude);
                streetLight.addLongitude(lightLongitude);
            }
        }
    }

    public void callStreetLamp(StreetLight streetLight,Double[] range){

        List<Integer> pages = new ArrayList<>(Arrays.asList(1,2,3,4,5));

        Flux.fromIterable(pages)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(this::setStreetLampMono)
                .sequential()
                .doOnNext(list-> {
                    try {
                        parseStreetLampAPIResult(streetLight,range,list);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                })
                .blockLast();
    }

    private String getSecurityLightJSON(String address) {

        return WebClient
                .create("https://apis.data.go.kr/6300000/GetScltListService1/getScltList1")
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("serviceKey", public_apiKey)
                        .queryParam("LNMADR", address)
                        .queryParam("pageNo", "1")
                        .queryParam("numOfRows", "2147483647")
                        .queryParam("type", "json")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private Mono<String> setStreetLampMono(int page) {

        return WebClient
                .builder()
                .baseUrl("https://api.odcloud.kr/api/15110054/v1/uddi:283207b1-f595-4c46-bbd5-838abeb18429")
                .exchangeStrategies(exchangeStrategies)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("page", page)
                        .queryParam("perPage", "9000")
                        .queryParam("returnType", "json")
                        .queryParam("serviceKey", public_apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }

    private void parseStreetLampAPIResult(StreetLight streetLight, Double[] range, String result) throws ParseException {

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(result);
        JSONArray list = (JSONArray) jsonObject.get("data");
        for (int i = 0; i < list.size(); i++) {
            JSONObject object = (JSONObject) list.get(i);
            lightLatitude = Double.valueOf((String) object.get("위도"));
            lightLongitude = Double.valueOf((String) object.get("경도"));
            if (lightLatitude >= range[0]
                    && lightLongitude >= range[1]
                    && lightLatitude <= range[2]
                    && lightLongitude <= range[3]) {
                streetLight.addLatitude(lightLatitude);
                streetLight.addLongitude(lightLongitude);
            }
        }
    }
}
