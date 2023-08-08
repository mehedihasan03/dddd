package net.celloscope.login.adapter.out.persistence;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.login.adapter.out.persistence.entity.LoginTrailEntity;
import net.celloscope.login.adapter.out.persistence.repository.*;
import net.celloscope.login.application.port.out.UserLoginPersistencePort;
import net.celloscope.login.domain.*;
import net.celloscope.utils.ExceptionHandlerUtil;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UserLoginPersistenceAdapter implements UserLoginPersistencePort {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final MfiRepository mfiRepository;
    private final BranchRepository branchRepository;
    private final LoginTrailRepository loginTrailRepository;
    private final ModelMapper modelMapper;

    public UserLoginPersistenceAdapter(UserRepository userRepository,
                                       UserRoleRepository userRoleRepository,
                                       RoleRepository roleRepository,
                                       EmployeeRepository employeeRepository,
                                       MfiRepository mfiRepository,
                                       BranchRepository branchRepository,
                                       LoginTrailRepository loginTrailRepository,
                                       ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.employeeRepository = employeeRepository;
        this.mfiRepository = mfiRepository;
        this.branchRepository = branchRepository;
        this.loginTrailRepository = loginTrailRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Mono<User> getUserInformation(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "User Information not found in database")))
                .map(userEntity -> modelMapper.map(userEntity, User.class))
                .doOnNext(value -> log.info("User from db : {}", value))
                .doOnRequest(value -> log.info("Request Received for user with username : {}", username))
                .doOnSuccess(entity -> log.info("user fetched successfully : {}", entity))
                .doOnError(err -> log.info("Found error while getting user from repository : {}", err.getLocalizedMessage()));
    }

    @Override
    public Mono<UserRole> getUserRoleInformation(String userOid) {
        return userRoleRepository.findByUserOid(userOid)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Information not found in database")))
                .map(userRoleEntity -> modelMapper.map(userRoleEntity, UserRole.class))
                .doOnNext(value -> log.info("User Role from db : {}", value))
                .doOnRequest(value -> log.info("Request Received for user role with user Oid : {}", userOid))
                .doOnSuccess(entity -> log.info("user Role Information fetched successfully : {}", entity))
                .doOnError(err -> log.info("Found error while getting user Role from repository : {}", err.getLocalizedMessage()));
    }

    @Override
    public Mono<Role> getRoleInformation(String roleOid) {
        return roleRepository.findByOid(roleOid)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Information not found in database")))
                .map(role -> modelMapper.map(role, Role.class))
                .doOnNext(value -> log.info("Role from db : {}", value))
                .doOnRequest(value -> log.info("Request Received for role with user Oid : {}", roleOid))
                .doOnSuccess(entity -> log.info("Role Information fetched successfully : {}", entity))
                .doOnError(err -> log.info("Found error while getting Role from repository : {}", err.getLocalizedMessage()));
    }

    @Override
    public Mono<Employee> getEmployeeInformation(String userOid) {
        return employeeRepository.findByUserOid(userOid)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Information not found in database")))
                .map(employee -> modelMapper.map(employee, Employee.class))
                .doOnNext(value -> log.info("Employee from db : {}", value))
                .doOnRequest(value -> log.info("Request Received for Employee with user Oid : {}", userOid))
                .doOnSuccess(entity -> log.info("Employee Information fetched successfully : {}", entity))
                .doOnError(err -> log.info("Found error while getting Employee from repository : {}", err.getLocalizedMessage()));
    }

    @Override
    public Mono<Mfi> getMfiInformation(String mfiOid) {
        return mfiRepository.findByOid(mfiOid)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Information not found in database")))
                .map(mfi -> modelMapper.map(mfi, Mfi.class))
                .doOnNext(value -> log.info("MFI from db : {}", value))
                .doOnRequest(value -> log.info("Request Received for MFI with MFI Oid : {}", mfiOid))
                .doOnSuccess(entity -> log.info("MFI Information fetched successfully : {}", entity))
                .doOnError(err -> log.info("Found error while getting MFI Data from repository : {}", err.getLocalizedMessage()));
    }

    @Override
    public Mono<Branch> getBranchInformation(String branchOid) {
        return branchRepository.findByOid(branchOid)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Information not found in database")))
                .map(branch -> modelMapper.map(branch, Branch.class))
                .doOnNext(value -> log.info("Branch from db : {}", value))
                .doOnRequest(value -> log.info("Request Received for Branch with Branch Oid : {}", branchOid))
                .doOnSuccess(entity -> log.info("Branch Information fetched successfully : {}", entity))
                .doOnError(err -> log.info("Found error while getting Branch Data from repository : {}", err.getLocalizedMessage()));
    }

    @Override
    public Mono<LoginTrail> saveLoginTrail(LoginTrail loginTrail) {
        return Mono.just(loginTrail)
                .map(lt -> modelMapper.map(lt, LoginTrailEntity.class))
                .flatMap(loginTrailRepository::save)
                .doOnNext(value -> log.info("Login Trail saved to DB : {}", value))
                .map(loginTrailEntity -> modelMapper.map(loginTrailEntity, LoginTrail.class))
                .doOnRequest(value -> log.info("Save Request Received for Login Trail : {}", loginTrail))
                .doOnSubscribe(subscription -> log.info("subscribe for save Login Trail to Db"))
                .doOnSuccess(entity -> log.info("Login Trail successfully saved to DB: {}", entity))
                .doOnError(err -> log.info("Found error while saving Login Trial to DB : {}", err.getLocalizedMessage()));
    }
}