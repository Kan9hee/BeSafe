package Bright.BeSafeProject.repository;

import Bright.BeSafeProject.entity.RouteEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RouteRepository extends CrudRepository<RouteEntity,Long> {
    boolean existsByEndLatitudeAndEndLongitude(double endLatitude, double endLongitude);
    boolean existsByStartLatitudeAndStartLongitude(double startLatitude, double startLongitude);

    List<RouteEntity> findByEmail(String email);

    default boolean existsAndNotDuplicate(RouteEntity routeEntity) {
        return !existsByStartLatitudeAndStartLongitude(routeEntity.getStartLatitude(), routeEntity.getStartLongitude())||
                existsByEndLatitudeAndEndLongitude(routeEntity.getEndLatitude(), routeEntity.getEndLongitude());
    }

    default RouteEntity saveIfNotDuplicate(RouteEntity routeEntity) {
        if (existsAndNotDuplicate(routeEntity)) {
            return save(routeEntity);
        }
        return null;
    }
}
