package net.celloscope.login.adapter.out.persistence.repository;

import net.celloscope.login.adapter.out.persistence.entity.BranchEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BranchRepository extends ReactiveCrudRepository<BranchEntity, String> {

    Mono<BranchEntity> findByOid(String branchOid);
}
