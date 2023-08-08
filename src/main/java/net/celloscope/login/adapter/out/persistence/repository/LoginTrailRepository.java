package net.celloscope.login.adapter.out.persistence.repository;

import net.celloscope.login.adapter.out.persistence.entity.LoginTrailEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginTrailRepository extends ReactiveCrudRepository<LoginTrailEntity, String> {
}
