package net.celloscope.login.adapter.out.persistence.repository;

import net.celloscope.login.adapter.out.persistence.entity.UserRoleEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRoleRepository extends ReactiveCrudRepository<UserRoleEntity, String> {

    Mono<UserRoleEntity> findByUserOid(String userOid);
}
