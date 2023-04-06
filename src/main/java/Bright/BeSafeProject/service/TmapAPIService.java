package Bright.BeSafeProject.service;

import Bright.BeSafeProject.model.Route;
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

    public String findAddress(Double[] location) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://apis.openapi.sk.com/tmap/geo/reversegeocoding?version=1&lat="+location[0]
                        +"&lon="+location[1]+"&coordType=WGS84GEO&addressType=A01&newAddressExtend=Y"))
                .header("accept", "application/json")
                .header("appKey", tmap_apiKey)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        return response.body();
    }

    public void callTmapRoute(Route route) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&callback=function"))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("appKey", tmap_apiKey)
                .method("POST", HttpRequest.BodyPublishers.ofString(
                        "{\"startX\":"+route.getStartLocation()[1]
                                +",\"startY\":"+route.getStartLocation()[0]
                                +",\"angle\":20,\"speed\":1,\"endPoiId\":\"10001\",\"endX\":"+route.getEndLocation()[1]
                                +",\"endY\":"+route.getEndLocation()[0]
                                +",\"reqCoordType\":\"WGS84GEO\",\"startName\":\"%EC%B6%9C%EB%B0%9C\",\"endName\":\"%EB%8F%84%EC%B0%A9\",\"searchOption\":\"10\",\"resCoordType\":\"WGS84GEO\",\"sort\":\"index\"}"
                ))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }
}
