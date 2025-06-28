package Bright.BeSafeProject.service;

import Bright.BeSafeProject.component.MongoCollectionComponent;
import Bright.BeSafeProject.dto.HistoryDTO;
import Bright.BeSafeProject.dto.LocationDTO;
import Bright.BeSafeProject.dto.ProfileDTO;
import Bright.BeSafeProject.entity.History;
import Bright.BeSafeProject.entity.LightNode;
import Bright.BeSafeProject.mapper.LightNodeMapper;
import Bright.BeSafeProject.repository.AccountRepository;
import Bright.BeSafeProject.repository.HistoryRepository;
import org.springframework.data.geo.Point;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataService {
    private final MongoCollectionComponent mongoCollectionComponent;
    private final AccountRepository accountRepository;
    private final HistoryRepository historyRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public Mono<Void> saveLightNodeData(String collection,
                                        List<LocationDTO> lightNodeList){
        LightNodeMapper mapper = LightNodeMapper.INSTANCE;
        List<List<LocationDTO>> batches = new ArrayList<>();
        for(int i=0;i<lightNodeList.size();i+=5000){
            int end = Math.min(i+5000,lightNodeList.size());
            batches.add(lightNodeList.subList(i,end));
        }
        return reactiveMongoTemplate
                .remove(new Query(), LightNode.class, collection)
                .thenMany(
                        Flux.fromIterable(batches)
                                .flatMap(batch ->
                                                Flux.fromIterable(batch)
                                                        .map(mapper::toEntity)
                                                        .collectList()
                                                        .flatMap(list ->
                                                                reactiveMongoTemplate.insert(list,collection).then()
                                                        ), 4)
                                .then()
                )
                .then();
    }

    public Mono<List<LocationDTO>> findLightNodeNearPath(List<LocationDTO> path){
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coordinates = path.stream()
                .map(location -> new Coordinate(location.longitude(),location.latitude()))
                .toArray(Coordinate[]::new);
        LineString lineString = geometryFactory.createLineString(coordinates);

        double bufferDistance = 10.0/111320.0;
        Polygon bufferPolygon = (Polygon) BufferOp.bufferOp(lineString,bufferDistance);

        List<Point> points = Arrays.stream(bufferPolygon.getCoordinates())
                .map(coord -> new Point(coord.x, coord.y))
                .collect(Collectors.toList());
        if(!points.get(0).equals(points.get(points.size()-1)))
            points.add(points.get(0));

        GeoJsonPolygon geoJsonPolygon = new GeoJsonPolygon(points);
        Criteria criteria = Criteria.where("location").within(geoJsonPolygon);
        Query query = new Query(criteria);

        return reactiveMongoTemplate.find(query, LightNode.class, mongoCollectionComponent.getActiveCollection())
                .map(LightNodeMapper.INSTANCE::toDto)
                .collectList();
    }

    public Mono<Void> saveServiceUsageHistory(Long accountId,
                                              float startX,
                                              float startY,
                                              float endX,
                                              float endY){
        History currentUsage = History.builder()
                .startX(startX)
                .startY(startY)
                .endX(endX)
                .endY(endY)
                .usedAt(LocalDateTime.now())
                .accountId(accountId)
                .build();
        return historyRepository.save(currentUsage).then();
    }

    public Mono<List<HistoryDTO>> getUsageList(Long accountId,
                                               int page,
                                               int size){
        int offset = page*size;
        return historyRepository.getRecentHistory(accountId,size,offset)
                .map(history -> new HistoryDTO(
                        history.getStartX(),
                        history.getStartY(),
                        history.getEndX(),
                        history.getEndY(),
                        history.getUsedAt()
                ))
                .collectList();
    }
}
