package Bright.BeSafeProject.repository;

import Bright.BeSafeProject.entity.History;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface HistoryRepository extends R2dbcRepository<History,Long> {
    @Query("""
        SELECT * FROM history
        WHERE account_id = :accountId
        ORDER BY used_at DESC
        LIMIT :limit OFFSET :offset
    """)
    Flux<History> getRecentHistory(
            @Param("accountId") Long accountId,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );
}
