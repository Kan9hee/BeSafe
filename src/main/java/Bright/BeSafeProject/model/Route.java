package Bright.BeSafeProject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    private String startAddress;
    private String endAddress;
    private Double[] startLocation;
    private Double[] endLocation;
    private ArrayList<Double> waypointLatitudes = new ArrayList<>();
    private ArrayList<Double> waypointLongitudes = new ArrayList<>();

    public boolean sameAddressCheck() { return (startAddress.equals(endAddress)); }
    public void addWaypointLatitude(Double Latitude){
        waypointLatitudes.add(Latitude);
    }
    public void addWaypointLongitude(Double Latitude){
        waypointLongitudes.add(Latitude);
    }
}
