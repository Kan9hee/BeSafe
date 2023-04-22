package Bright.BeSafeProject.service;

import Bright.BeSafeProject.model.Route;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class TmapAPIService {

    private JSONParser jsonParser;

    @Value("${TMAP_APPKEY}")
    private String tmap_apiKey;

    public String findAddress(Double[] location) throws IOException, InterruptedException, ParseException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://apis.openapi.sk.com/tmap/geo/reversegeocoding?version=1&lat="+location[0]
                        +"&lon="+location[1]+"&coordType=WGS84GEO&addressType=A01&newAddressExtend=Y"))
                .header("accept", "application/json")
                .header("appKey", tmap_apiKey)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(response.body());
        JSONObject x1 = (JSONObject) jsonObject.get("addressInfo");
        String address = (String) x1.get("fullAddress");
        return address;
    }

    public void callTmapRoute(Route route) throws IOException, InterruptedException, ParseException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&callback=function"))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("appKey", tmap_apiKey)
                .method("POST", HttpRequest.BodyPublishers.ofString(route.getHttpRequestRoute()))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        jsonParser = new JSONParser();
        JSONArray save = null;
        JSONObject jsonObject = (JSONObject) jsonParser.parse(response.body());
        JSONArray waypoints = (JSONArray) jsonObject.get("features");
        for (int i = 0; i < waypoints.size(); i++) {
            JSONObject element = (JSONObject) waypoints.get(i);
            JSONObject geometry = (JSONObject) element.get("geometry");
            if(geometry.get("type").equals("LineString")){
                JSONArray list = (JSONArray) geometry.get("coordinates");
                for (int num = 0; num < list.size(); num++){
                    JSONArray waypoint = (JSONArray) list.get(num);
                    if(save==null||!save.equals(waypoint)) {
                        route.addWaypointLatitude((Double) waypoint.get(1));
                        route.addWaypointLongitude((Double) waypoint.get(0));
                        save=waypoint;
                    }
                }
            }
        }
    }
}
