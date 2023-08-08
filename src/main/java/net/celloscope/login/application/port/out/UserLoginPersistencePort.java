package net.celloscope.login.application.port.out;

import net.celloscope.login.domain.*;
import reactor.core.publisher.Mono;

public interface UserLoginPersistencePort {
    Mono<User> getUserInformation(String username);
    Mono<UserRole> getUserRoleInformation(String userOid);
    Mono<Role> getRoleInformation(String roleOid);
    Mono<Employee> getEmployeeInformation(String userOid);
    Mono<Mfi> getMfiInformation(String mfiOid);
    Mono<Branch> getBranchInformation(String branchOid);
    Mono<LoginTrail> saveLoginTrail(LoginTrail loginTrail);
}
