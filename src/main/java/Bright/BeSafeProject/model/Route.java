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
    private Double[] showRange;
    private ArrayList<Double> searchRange = new ArrayList<>();
    private ArrayList<Double> waypointLatitudes = new ArrayList<>();
    private ArrayList<Double> waypointLongitudes = new ArrayList<>();

    public void setShowRange(){
        showRange= new Double[]{startLocation[0], startLocation[1], endLocation[0], endLocation[1]};
        if(showRange[0]>showRange[2]){
            showRange[0]=endLocation[0];
            showRange[2]=startLocation[0];
        }
        if(showRange[1]>showRange[3]){
            showRange[1]=endLocation[1];
            showRange[3]=startLocation[1];
        }
    }
    public boolean sameAddressCheck() { return (startAddress.equals(endAddress)); }
    public void addWaypointLatitude(Double Latitude){
        waypointLatitudes.add(Latitude);
    }
    public void addWaypointLongitude(Double Latitude){
        waypointLongitudes.add(Latitude);
    }
}
