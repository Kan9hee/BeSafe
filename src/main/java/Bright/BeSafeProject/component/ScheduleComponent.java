package Bright.BeSafeProject.component;

import Bright.BeSafeProject.config.ConstantNumberConfig;
import Bright.BeSafeProject.dto.LocationDTO;
import Bright.BeSafeProject.dto.apiResponse.StreetResponseDTO;
import Bright.BeSafeProject.service.DataService;
import Bright.BeSafeProject.service.ExternalApiService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class ScheduleComponent {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleComponent.class);
    private final ConstantNumberConfig constantNumberConfig;
    private final DataService dataService;
    private final ExternalApiService externalApiService;
    private final MongoCollectionComponent mongoCollectionComponent;

    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleUpdateAndSwap() {
        updateAndSwapLightNodeDB()
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        unused -> {},
                        error -> logger.error("공공데이터 API 호출 스케줄 작업 실패",error)
                );
    }

    public Mono<Void> updateAndSwapLightNodeDB(){
        return reloadAllLightNodeData()
                .flatMap(data ->
                        dataService.saveLightNodeData(mongoCollectionComponent.getPassiveCollection(),data))
                .doOnSuccess(next -> mongoCollectionComponent.swapActiveCollection())
                .then();
    }

    private Mono<List<LocationDTO>> reloadAllLightNodeData(){
        return externalApiService.callStreetLightData(constantNumberConfig.getFirstPage(),constantNumberConfig.getPageSize())
                .flatMap(firstResponse -> {
                    List<LocationDTO> firstPageData = extractLocationDTOs(firstResponse);
                    int totalPage = firstResponse.totalCount()/constantNumberConfig.getPageSize()
                            +(firstResponse.totalCount()%constantNumberConfig.getPageSize() > 0
                            ?constantNumberConfig.getFirstPage()
                            :0);
                    if(totalPage<=constantNumberConfig.getFirstPage())
                        return Mono.just(firstPageData);

                    List<Integer> remainingPages = IntStream.rangeClosed(constantNumberConfig.getParallelStartPage(),totalPage).boxed().toList();
                    return Flux.fromIterable(remainingPages)
                            .parallel()
                            .runOn(Schedulers.parallel())
                            .flatMap(page -> externalApiService.callStreetLightData(page,constantNumberConfig.getPageSize()))
                            .map(this::extractLocationDTOs)
                            .sequential()
                            .flatMapIterable(list -> list)
                            .collectList()
                            .map(restData -> {
                                List<LocationDTO> allLocations = new ArrayList<>(firstPageData);
                                allLocations.addAll(restData);
                                return allLocations;
                            });
                });
    }

    private List<LocationDTO> extractLocationDTOs(StreetResponseDTO response){
        if(response==null || response.data()==null)
            return Collections.emptyList();
        return response
                .data()
                .stream()
                .map(data -> new LocationDTO(data.longitude(),data.latitude()))
                .collect(Collectors.toList());
    }
}
