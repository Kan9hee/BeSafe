package Bright.BeSafeProject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    private String startAddress;
    private String endAddress;
    private Double[] startLocation;
    private Double[] endLocation;

    public boolean sameAddressCheck(){
        return !(startAddress.equals(endAddress));
    }
}
