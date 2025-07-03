package Bright.BeSafeProject.service;

import Bright.BeSafeProject.component.MongoCollectionComponent;
import Bright.BeSafeProject.config.ApiParamConfig;
import Bright.BeSafeProject.config.ConstantNumberConfig;
import Bright.BeSafeProject.dto.HistoryDTO;
import Bright.BeSafeProject.dto.LocationDTO;
import Bright.BeSafeProject.dto.ProfileDTO;
import Bright.BeSafeProject.entity.History;
import Bright.BeSafeProject.entity.LightNode;
import Bright.BeSafeProject.exception.CustomException;
import Bright.BeSafeProject.exception.ErrorCode;
import Bright.BeSafeProject.mapper.LightNodeMapper;
import Bright.BeSafeProject.repository.AccountRepository;
import Bright.BeSafeProject.repository.HistoryRepository;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class DataService {
    private final ApiParamConfig apiParamConfig;
    private final ConstantNumberConfig constantNumberConfig;
    private final MongoCollectionComponent mongoCollectionComponent;
    private final HistoryRepository historyRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public Mono<Void> saveLightNodeData(String collection,
                                        List<LocationDTO> lightNodeList){
        LightNodeMapper mapper = LightNodeMapper.INSTANCE;

        List<List<LocationDTO>> batches = new ArrayList<>();
        for(int i=0;i<lightNodeList.size();i+=constantNumberConfig.getPageSize()){
            int end = Math.min(i+constantNumberConfig.getPageSize(),lightNodeList.size());
            batches.add(lightNodeList.subList(i,end));
        }

        return reactiveMongoTemplate
                .remove(new Query(), LightNode.class, collection)
                .onErrorResume(e -> {
                    log.error(e.getMessage());
                    return Mono.error(new CustomException(ErrorCode.LIGHT_COLLECTION_UPDATE_FAILED));
                })
                .thenMany(
                        Flux.fromIterable(batches)
                                .flatMap(batch ->
                                                Flux.fromIterable(batch)
                                                        .map(mapper::toEntity)
                                                        .collectList()
                                                        .flatMap(list ->
                                                                reactiveMongoTemplate.insert(list,collection)
                                                                        .doOnError(e -> log.info(e.getMessage()))
                                                                        .onErrorResume(e -> {
                                                                            log.error(e.getMessage());
                                                                            return Mono.error(new CustomException(ErrorCode.LIGHT_COLLECTION_UPDATE_FAILED));
                                                                        })
                                                                        .then()
                                                        ), constantNumberConfig.getConcurrency())
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

        double bufferDistance = constantNumberConfig.getRadius()/constantNumberConfig.getMetersPerDegree();
        Polygon bufferPolygon = (Polygon) BufferOp.bufferOp(lineString,bufferDistance);

        List<Point> points = Arrays.stream(bufferPolygon.getCoordinates())
                .map(coord -> new Point(coord.x, coord.y))
                .collect(Collectors.toList());
        if(!points.get(0).equals(points.get(points.size()-1)))
            points.add(points.get(0));

        GeoJsonPolygon geoJsonPolygon = new GeoJsonPolygon(points);
        Criteria criteria = Criteria.where(apiParamConfig.getLightNodeLocation()).within(geoJsonPolygon);
        Query query = new Query(criteria);

        return reactiveMongoTemplate.find(query, LightNode.class, mongoCollectionComponent.getActiveCollection())
                .onErrorResume(e -> {
                    log.error(e.getMessage());
                    return Mono.error(new CustomException(ErrorCode.LIGHT_COLLECTION_SEARCH_FAILED));
                })
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

        return historyRepository.save(currentUsage)
                .onErrorResume(e -> {
                    log.error(e.getMessage());
                    return Mono.error(new CustomException(ErrorCode.USAGE_HISTORY_SAVE_FAILED));
                })
                .then();
    }

    public Mono<List<HistoryDTO>> getUsageList(Long accountId,
                                               int page,
                                               int size){
        int offset = page*size;
        return historyRepository.getRecentHistory(accountId,size,offset)
                .onErrorResume(e -> {
                    log.error(e.getMessage());
                    return Mono.error(new CustomException(ErrorCode.USAGE_HISTORY_CALL_FAILED));
                })
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
