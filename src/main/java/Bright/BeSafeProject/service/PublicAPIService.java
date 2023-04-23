package Bright.BeSafeProject.service;

import Bright.BeSafeProject.model.StreetLight;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    private Double streetLatitude;
    private Double streetLongitude;

    @Value("${PUBLIC_DATA_KEY}")
    private String public_apiKey;

    public void callSecurityLight(StreetLight streetLight,String address) throws IOException, ParseException {
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
        System.out.println("방범등 API 호출, Response code: " + conn.getResponseCode());
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
            streetLight.addLatitude((Double) object.get("LATITUDE"));
            streetLight.addLongitude((Double) object.get("LONGITUDE"));
        }
    }

    public void callStreetLamp(StreetLight streetLight,Double[] range) throws IOException, ParseException {

        List<Integer> pages = new ArrayList<>(Arrays.asList(1,2,3,4,5));
        List<String> streetLampLists=new ArrayList<>();

        Flux.fromIterable(pages)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(this::streetLampSearch)
                .sequential()
                .subscribe(list->streetLampLists.add(list));

        for(int page = 0; page < pages.size(); page++) {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(streetLampLists.get(page));
            JSONArray list = (JSONArray) jsonObject.get("data");
            for (int i = 0; i < list.size(); i++) {
                JSONObject object = (JSONObject) list.get(i);
                streetLatitude = Double.valueOf((String) object.get("위도"));
                streetLongitude = Double.valueOf((String) object.get("경도"));
                streetLight.addLatitude(streetLatitude);
                streetLight.addLongitude(streetLongitude);
            }
        }
    }

    private Mono<String> streetLampSearch(int page) {
        System.out.println("가로등 API 호출: "+page+"/5");
        return WebClient.create("https://api.odcloud.kr/api/15110054/v1/uddi:283207b1-f595-4c46-bbd5-838abeb18429")
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
}
