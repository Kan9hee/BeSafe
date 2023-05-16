package Bright.BeSafeProject.dto;


import Bright.BeSafeProject.entity.RouteEntity;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

@Getter
@Setter
public class RouteDTO {
    private String email;
    private String startAddress;
    private Double[] startLocation;
    private String endAddress;
    private Double[] endLocation;
    private Double[] passLocation;

    public RouteDTO(String email,
                    String startAddress, Double[] start,
                    String endAddress, Double[] end,
                    Double[] pass){
        this.email=email;
        this.startAddress=startAddress;
        this.startLocation=start;
        this.endAddress=endAddress;
        this.endLocation=end;
        this.passLocation=pass != null ? pass : new Double[]{-1.0,-1.0};
    }

    public RouteDTO(JSONObject resultAddress) {
        this.email= (String) resultAddress.get("email");
        this.startAddress= (String) resultAddress.get("startAddress");
        this.startLocation= new Double[]{(Double) resultAddress.get("startLatitude"),(Double) resultAddress.get("startLongitude")};
        this.endAddress= (String) resultAddress.get("endAddress");
        this.endLocation= new Double[]{(Double) resultAddress.get("endLatitude"),(Double) resultAddress.get("endLongitude")};
        this.passLocation= new Double[]{(Double) resultAddress.get("passLatitude"),(Double) resultAddress.get("passLongitude")};
    }

    public RouteEntity toEntity(){return new RouteEntity(email,startAddress,startLocation,endAddress,endLocation,passLocation);}
}
