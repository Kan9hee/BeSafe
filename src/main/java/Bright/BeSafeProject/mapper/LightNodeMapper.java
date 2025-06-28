package Bright.BeSafeProject.mapper;

import Bright.BeSafeProject.dto.LocationDTO;
import Bright.BeSafeProject.entity.LightNode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LightNodeMapper {
    LightNodeMapper INSTANCE = Mappers.getMapper(LightNodeMapper.class);

    @Mapping(target = "longitude", expression = "java(lightNode.getLocation().getX())")
    @Mapping(target = "latitude", expression = "java(lightNode.getLocation().getY())")
    LocationDTO toDto(LightNode lightNode);

    default LightNode toEntity(LocationDTO locationDTO){
        return new LightNode(locationDTO.longitude(), locationDTO.latitude());
    }
}
