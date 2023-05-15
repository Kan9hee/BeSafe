package Bright.BeSafeProject.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class RouteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String startAddress;
    @Column(nullable = false)
    private Double startLatitude;
    @Column(nullable = false)
    private Double startLongitude;
    @Column(nullable = false)
    private String endAddress;
    @Column(nullable = false)
    private Double endLatitude;
    @Column(nullable = false)
    private Double endLongitude;
    @Column(nullable = true)
    private Double passLatitude;
    @Column(nullable = true)
    private Double passLongitude;

    public RouteEntity(){}

    public RouteEntity(String email,
                       String startAddress, Double[] start,
                       String endAddress, Double[] end,
                       Double[] pass){
        this.email=email;
        this.startAddress=startAddress;
        this.startLatitude=start[0];
        this.startLongitude=start[1];
        this.endAddress=endAddress;
        this.endLatitude=end[0];
        this.endLongitude=end[1];
        this.passLatitude=pass[0] != -1 ? pass[0] : null;
        this.passLongitude=pass[1] != -1 ? pass[1] : null;
    }
}
