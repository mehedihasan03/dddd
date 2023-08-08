package net.celloscope.login.adapter.out.persistence.repository;

import net.celloscope.login.adapter.out.persistence.entity.MfiEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MfiRepository extends ReactiveCrudRepository<MfiEntity, String> {

    Mono<MfiEntity> findByOid(String mfiOid);
}
