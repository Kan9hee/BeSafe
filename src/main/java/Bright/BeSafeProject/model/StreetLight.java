package Bright.BeSafeProject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreetLight {
    private ArrayList<Double> latitudeList = new ArrayList<>();
    private ArrayList<Double> longitudeList = new ArrayList<>();

    public void addLatitude(Double Latitude){
        latitudeList.add(Latitude);
    }
    public void addLongitude(Double Longitude){
        longitudeList.add(Longitude);
    }
    public void setRangeWithRoute(Double[] routeRange){
        for(int i=0;i<latitudeList.size();) {
            if (latitudeList.get(i) >= routeRange[0] && longitudeList.get(i) >= routeRange[1]
                    && latitudeList.get(i) <= routeRange[2] && longitudeList.get(i) <= routeRange[3]) {
                i++;
            }else{
                latitudeList.remove(i);
                longitudeList.remove(i);
            }
        }
    }
}
