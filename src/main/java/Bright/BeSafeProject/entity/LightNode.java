package Bright.BeSafeProject.entity;

import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

@Getter
public class LightNode {
    @Id
    private ObjectId _id;

    private GeoJsonPoint location;

    protected LightNode(){}

    public LightNode(double longitude,double latitude){
        this._id = new ObjectId();
        this.location = new GeoJsonPoint(longitude, latitude);
    }
}
