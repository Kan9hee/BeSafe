package Bright.BeSafeProject.service;

import Bright.BeSafeProject.model.StreetLight;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

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
        for(int page=1;page<6;page++) {
            StringBuilder urlBuilder = new StringBuilder("https://api.odcloud.kr/api/15110054/v1/uddi:283207b1-f595-4c46-bbd5-838abeb18429");
            urlBuilder.append("?" + URLEncoder.encode("page", "UTF-8") + "=" + page);
            urlBuilder.append("&" + URLEncoder.encode("perPage", "UTF-8") + "=" + URLEncoder.encode("9000", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("returnType", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + public_apiKey);
            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");
            System.out.println("가로등 API 호출 (" + page + "/5), Response code: " + conn.getResponseCode());
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
}
