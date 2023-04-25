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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PublicAPIService {

    private Double lightLatitude;
    private Double lightLongitude;

    @Value("${PUBLIC_DATA_KEY}")
    private String public_apiKey;

    public void callSecurityLight(StreetLight streetLight,Double[] range,String address) throws IOException, ParseException {
        StringBuilder urlBuilder = new StringBuilder("https://apis.data.go.kr/6300000/GetScltListService1/getScltList1");
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + public_apiKey);
        urlBuilder.append("&" + URLEncoder.encode("LNMADR", "UTF-8") + "=" + URLEncoder.encode(address, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("2147483647", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8"));
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(sb.toString());
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
                .flatMap(this::streetLampSearch)
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

    private Mono<String> streetLampSearch(int page) {

        ExchangeStrategies exchangeStrategies=ExchangeStrategies.builder()
                .codecs(configure->configure.defaultCodecs().maxInMemorySize(-1))
                .build();

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

        System.out.println("parse complete");
    }
}
