package Bright.BeSafeProject.repository;

import Bright.BeSafeProject.entity.Account;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface AccountRepository extends R2dbcRepository<Account, Long> {
    Mono<Account> findByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
    Mono<Long> deleteByEmail(String email);
}
