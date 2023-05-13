package Bright.BeSafeProject.repository;

import Bright.BeSafeProject.entity.RouteEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends CrudRepository<RouteEntity,Long> {
}
