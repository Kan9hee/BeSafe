package Bright.BeSafeProject.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Controller
public class MapController {

    @Value("${TMAP_APPKEY}")
    private String tmap_apiKey;

    @Value("${PUBLIC_DATA_KEY}")
    private String public_apiKey;

    @GetMapping(value = "/map",produces = "application/json;charset=utf8")
    public String Map() throws IOException, InterruptedException, URISyntaxException {

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

        String streetLightUrl = "https://api.odcloud.kr/api/15060378/v1/uddi:fa048c66-4cea-4294-ab54-d5802f2a116f?page=1&perPage=10&serviceKey="+public_apiKey; /*URL*/
        System.out.println(streetLightUrl);
        URI streetLight=new URI(streetLightUrl);
        RestTemplate restTemplate = new RestTemplate();
        String jsonStreetLight = restTemplate.getForObject(streetLight, String.class);
        System.out.println(jsonStreetLight);

        String securityLightUrl = "http://api.data.go.kr/openapi/tn_pubr_public_scrty_lmp_api?s_page=1&s_list=10&serviceKey="+public_apiKey; /*URL*/
        System.out.println(securityLightUrl);
        URI securityLight=new URI(securityLightUrl);
        String jsonSecurityLight = restTemplate.getForObject(securityLight, String.class);
        System.out.println(jsonSecurityLight);

        return "tmapExam";
    }

    //@PostMapping(value = "/map")
    public String result(){return "tmapExam";}
}
