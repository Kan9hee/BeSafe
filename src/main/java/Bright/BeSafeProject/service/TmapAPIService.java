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
import java.util.ArrayList;

@Service
public class TmapAPIService {

    @Value("${TMAP_APPKEY}")
    private String tmap_apiKey;

    public String findAddress(Double[] location) throws IOException, InterruptedException, ParseException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://apis.openapi.sk.com/tmap/geo/reversegeocoding?version=1&lat="+location[0]
                        +"&lon="+location[1]+"&coordType=WGS84GEO&addressType=A00&newAddressExtend=Y"))
                .header("accept", "application/json")
                .header("appKey", tmap_apiKey)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(response.body());
        JSONObject x1 = (JSONObject) jsonObject.get("addressInfo");
        return (String) x1.get("fullAddress");
    }

    public void callTmapRoute(Route route) throws IOException, InterruptedException, ParseException {

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&callback=function"))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("appKey", tmap_apiKey)
                .method("POST", HttpRequest.BodyPublishers.ofString(getHttpRequestRoute(route)))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());

        JSONParser jsonParser = new JSONParser();
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

    private String getHttpRequestRoute(Route route){
        resetInPath(route);
        String HttpRequest = "{\"startX\":"+route.getStartLocation()[1]
                +",\"startY\":"+route.getStartLocation()[0]
                +",\"angle\":20,\"speed\":1,\"endPoiId\":\"10001\",\"endX\":"+route.getEndLocation()[1]
                +",\"endY\":"+route.getEndLocation()[0];
        if(route.getPassLocation()!=null){
            HttpRequest+=",\"passList\":\""+route.getPassLocation()[0]+","+route.getPassLocation()[1]+"\"";
        }
        HttpRequest+=",\"reqCoordType\":\"WGS84GEO\",\"startName\":\"%EC%B6%9C%EB%B0%9C\",\"endName\":\"%EB%8F%84%EC%B0%A9\",\"searchOption\":\"10\",\"resCoordType\":\"WGS84GEO\",\"sort\":\"index\"}";
        return HttpRequest;
    }

    private void resetInPath(Route route){
        route.setShowRange(null);
        route.setSearchRange(new ArrayList<>());
        route.setWaypointLatitudes(new ArrayList<>());
        route.setWaypointLongitudes(new ArrayList<>());
    }
}
