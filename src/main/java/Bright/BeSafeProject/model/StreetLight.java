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
}
