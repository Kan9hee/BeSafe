package Bright.BeSafeProject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class TmapAPIService {

    @Value("${TMAP_APPKEY}")
    private String tmap_apiKey;

    public void callTmapRoute() throws IOException, InterruptedException {
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
    }
}
