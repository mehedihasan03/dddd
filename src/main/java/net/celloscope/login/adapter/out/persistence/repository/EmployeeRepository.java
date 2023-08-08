package net.celloscope.login.adapter.out.persistence.repository;

import net.celloscope.login.adapter.out.persistence.entity.EmployeeEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface EmployeeRepository extends ReactiveCrudRepository<EmployeeEntity, String> {

    Mono<EmployeeEntity> findByUserOid(String userOid);
}
