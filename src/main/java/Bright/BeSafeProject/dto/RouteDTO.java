package Bright.BeSafeProject.dto;


import Bright.BeSafeProject.entity.RouteEntity;

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

    public RouteEntity toEntity(){return new RouteEntity(email,startAddress,startLocation,endAddress,endLocation,passLocation);}
}
