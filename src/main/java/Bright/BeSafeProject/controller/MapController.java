package Bright.BeSafeProject.controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Controller
public class MapController {

    @Value("${TMAP_APPKEY}")
    private String tmap_apiKey;

    @Value("${PUBLIC_DATA_KEY}")
    private String public_apiKey;

    @GetMapping(value = "/map")
    public String Map() throws IOException, InterruptedException, URISyntaxException, ParseException {

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&callback=function"))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("appKey", tmap_apiKey)
                .method("POST", HttpRequest.BodyPublishers.ofString(
                        "{\"startX\":127.14644790564539,\"startY\":36.81044675656011,\"angle\":20,\"speed\":1,\"endPoiId\":\"10001\",\"endX\":127.15304613980264,\"endY\":36.81207191900239,\"reqCoordType\":\"WGS84GEO\",\"startName\":\"%EC%B6%9C%EB%B0%9C\",\"endName\":\"%EB%8F%84%EC%B0%A9\",\"searchOption\":\"10\",\"resCoordType\":\"WGS84GEO\",\"sort\":\"index\"}"
                ))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());


        //String streetLightUrl = "https://api.odcloud.kr/api/15060378/v1/uddi:fa048c66-4cea-4294-ab54-d5802f2a116f?page=1&perPage=10&serviceKey="+public_apiKey; /*URL*/
        //System.out.println(streetLightUrl);
        //URI streetLight=new URI(streetLightUrl);
        //RestTemplate restTemplate = new RestTemplate();
        //restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        //String jsonStreetLight = restTemplate.getForObject(streetLight, String.class);
        //System.out.println(jsonStreetLight);

        StringBuilder urlBuilder = new StringBuilder("https://apis.data.go.kr/6300000/GetScltListService1/getScltList1");
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=" + public_apiKey);
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("20", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("type","UTF-8") + "=" + URLEncoder.encode("json", "UTF-8"));
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
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
        JSONObject jsonObject = (JSONObject)jsonParser.parse(sb.toString());
        JSONObject x1 = (JSONObject)jsonObject.get("response");
        JSONObject x2 = (JSONObject)x1.get("body");
        JSONObject x3 = (JSONObject)x2.get("items");
        JSONArray list = (JSONArray)x3.get("item");
        for(int i=0;i<list.size();i++){
            JSONObject streetLight = (JSONObject) list.get(i);
            System.out.println("LATITUDE: "+streetLight.get("LATITUDE")+" ,LONGITUDE: "+streetLight.get("LONGITUDE"));
        }
        return "tmapExam";
    }

    //@PostMapping(value = "/map")
    public String result(){return "tmapExam";}
}
