package net.celloscope.login.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.login.adapter.in.dto.request.UserRequestDto;
import net.celloscope.login.adapter.in.dto.response.UserLoginResponseDto;
import net.celloscope.login.application.port.in.UserLoginUseCase;
import net.celloscope.login.application.port.out.UserLoginPersistencePort;
import net.celloscope.login.domain.*;
import net.celloscope.utils.ExceptionHandlerUtil;
import net.celloscope.utils.JwtServiceUtils;
import net.celloscope.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;

@Service
@Slf4j
public class UserLoginService implements UserLoginUseCase {

    private final UserLoginPersistencePort userLoginPersistencePort;
    private final JwtServiceUtils jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtils redisUtils;
    private final Clock clock;

    public UserLoginService(UserLoginPersistencePort userLoginPersistencePort,
                            JwtServiceUtils jwtService,
                            @Qualifier("encoder") PasswordEncoder passwordEncoder,
                            RedisUtils redisUtils,
                            Clock clock) {
        this.userLoginPersistencePort = userLoginPersistencePort;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.redisUtils = redisUtils;
        this.clock = clock;
    }

    @Override
    public Mono<UserLoginResponseDto> loginUser(UserRequestDto requestDto) {
        return userLoginPersistencePort.getUserInformation(requestDto.getUsername())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "User not found with this username : " + requestDto.getUsername())))
                .flatMap(user -> checkActivityAndCredentials(requestDto, user))
                .doOnNext(responseDto -> log.info("Login Response : {}", responseDto))
                .doOnRequest(value -> log.info("User login request for : {}", requestDto))
                .doOnSuccess(response -> log.info("Login response for user is : {}", response))
                .doOnError(err -> log.info("Found error while an user logged in : {}", err.getLocalizedMessage()));
    }

    @Override
    public Mono<String> logOutUser(String token, String ip) {
        return Mono.just(token)
                .map(redisUtils::checkRedisData)
                .map(data -> buildLoginTrailForLogout(data, ip))
                .flatMap(userLoginPersistencePort::saveLoginTrail)
                .map(loginTrail -> redisUtils.deleteRedisData(token))
                .doOnNext(s -> log.info("Logout Response : {}", s))
                .doOnRequest(value -> log.info("User logout request for : {}", value))
                .doOnSuccess(response -> log.info("Logout response for user is : {}", response))
                .doOnError(err -> log.info("Found error while an user logged out : {}", err.getLocalizedMessage()));
    }

    private Mono<UserLoginResponseDto> checkActivityAndCredentials(UserRequestDto requestDto, User user) {
        if (isCredentialsMatched(requestDto.getPassword(), user.getPassword())) {
            log.info("Password matched for this user : {}", user.getUsername());
            if (user.getStatus().equals("Active")) {
                log.info("Check User activity for this user : {}", user.getUsername());
                return getAllUserRelatedDataV2(requestDto, user);
            } else {
                log.error("User is not currently active for login.");
                return Mono.error(new ExceptionHandlerUtil(HttpStatus.FORBIDDEN, "User is not currently active"));
            }
        } else {
            log.error("Password didn't match for username: {} ", requestDto.getUsername());
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.FORBIDDEN, "Invalid password for user : {}", requestDto.getUsername()));
        }
    }

    private Mono<UserLoginResponseDto> getAllUserRelatedDataV2(UserRequestDto requestDto, User user) {
        return getUserRoleData(user)
                .zipWith(getEmployeeData(user.getOid()))
                .flatMap(tuple2 -> getRoleData(tuple2.getT1().getRoleOid())
                        .zipWith(Mono.just(tuple2)))
                .flatMap(tuple2 -> {
                            if (tuple2.getT2().getT2().getMfiOid() != null) {
                                return getMfiData(tuple2.getT2().getT2().getMfiOid())
                                        .flatMap(mfi -> {
                                            if (tuple2.getT2().getT2().getBranchOid() != null) {
                                                return getBranchData(tuple2.getT2().getT2().getBranchOid())
                                                        .zipWith(saveLoginTrailData(requestDto, user))
                                                        .flatMap(tpl2 -> {
                                                            redisUtils.saveRedisData(buildRedisDataForLoginUser(user, tuple2.getT2().getT1(), tuple2.getT1(), tuple2.getT2().getT2(), mfi, tpl2.getT1()));
                                                            return buildLoginResponseDto(user, tuple2.getT1(), tuple2.getT2().getT2());
                                                        });
                                            } else {
                                                saveLoginTrailData(requestDto, user).subscribeOn(Schedulers.immediate()).subscribe();
                                                redisUtils.saveRedisData(buildRedisDataForLoginMfi(user, tuple2.getT2().getT1(), tuple2.getT1(), tuple2.getT2().getT2(), mfi));
                                                return buildLoginResponseDto(user, tuple2.getT1(), tuple2.getT2().getT2());
                                            }
                                        });
                            } else {
                                saveLoginTrailData(requestDto, user).subscribeOn(Schedulers.immediate()).subscribe();
                                redisUtils.saveRedisData(buildRedisDataForLoginMra(user, tuple2.getT2().getT1(), tuple2.getT1(), tuple2.getT2().getT2()));
                                return buildLoginResponseDto(user, tuple2.getT1(), tuple2.getT2().getT2());
                            }
                        }
                );
    }

    private LoginTrail buildLoginTrailForLogout(RedisTempData data, String ip) {
        return LoginTrail.builder()
                .userOid(data.getUserOid())
                .type("sign_out")
                .sourceIp(ip)
                .inOutTime(Timestamp.from(Instant.now(clock)))
                .build();
    }

    private RedisTempData buildRedisDataForLoginMra(User user, UserRole userRole, Role role, Employee employee) {
        return RedisTempData.builder()
                .userOid(user.getOid())
                .username(user.getUsername())
                .token(jwtService.generateToken(user.getOid(), user.getUsername(), role.getRoleName()))
                .userStatus(user.getStatus())
                .userRoleOid(userRole.getOid())
                .roleOid(role.getOid())
                .roleName(role.getRoleName())
                .roleStatus(role.getStatus())
                .employeeOid(employee.getOid())
                .fullName(employee.getFullName())
                .designation(employee.getDesignation())
                .mobileNumber(employee.getMobileNumber())
                .email(employee.getEmail())
                .employeeStatus(employee.getStatus())
                .build();
    }

    private RedisTempData buildRedisDataForLoginMfi(User user, UserRole userRole, Role role, Employee employee, Mfi mfi) {
        return RedisTempData.builder()
                .userOid(user.getOid())
                .username(user.getUsername())
                .token(jwtService.generateToken(user.getOid(), user.getUsername(), role.getRoleName()))
                .userStatus(user.getStatus())
                .userRoleOid(userRole.getOid())
                .roleOid(role.getOid())
                .roleName(role.getRoleName())
                .roleStatus(role.getStatus())
                .employeeOid(employee.getOid())
                .fullName(employee.getFullName())
                .designation(employee.getDesignation())
                .mobileNumber(employee.getMobileNumber())
                .email(employee.getEmail())
                .employeeStatus(employee.getStatus())
                .mfiOid(mfi.getOid())
                .mfiName(mfi.getMfiName())
                .mfiLicenseNumber(mfi.getLicenseNumber())
                .mfiStatus(mfi.getStatus())
                .build();

    }

    private RedisTempData buildRedisDataForLoginUser(User user, UserRole userRole, Role role, Employee employee, Mfi mfi, Branch branch) {
        return RedisTempData.builder()
                .userOid(user.getOid())
                .username(user.getUsername())
                .token(jwtService.generateToken(user.getOid(), user.getUsername(), role.getRoleName()))
                .userStatus(user.getStatus())
                .userRoleOid(userRole.getOid())
                .roleOid(role.getOid())
                .roleName(role.getRoleName())
                .roleStatus(role.getStatus())
                .employeeOid(employee.getOid())
                .fullName(employee.getFullName())
                .designation(employee.getDesignation())
                .mobileNumber(employee.getMobileNumber())
                .email(employee.getEmail())
                .employeeStatus(employee.getStatus())
                .branchOid(branch.getOid())
                .branchCode(branch.getBranchCode())
                .branchName(branch.getBranchName())
                .branchStatus(branch.getStatus())
                .mfiOid(mfi.getOid())
                .mfiName(mfi.getMfiName())
                .mfiLicenseNumber(mfi.getLicenseNumber())
                .mfiStatus(mfi.getStatus())
                .build();
    }

    private LoginTrail buildLoginTrail(UserRequestDto requestDto, User user) {
        return LoginTrail.builder()
                .userOid(user.getOid())
                .type("sign_in")
                .sourceIp(requestDto.getSourceIp())
                .inOutTime(Timestamp.from(Instant.now(clock)))
                .build();
    }

    private Mono<UserLoginResponseDto> buildLoginResponseDto(User user, Role role, Employee employee) {
        return Mono.just(
                UserLoginResponseDto.builder()
                        .username(user.getUsername())
                        .passwordResetRequired(user.getPasswordResetRequired())
                        .token(jwtService.generateToken(user.getOid(), user.getUsername(), role.getRoleName()))
                        .roleName(role.getRoleName())
                        .fullName(employee.getFullName())
                        .mobileNumber(employee.getMobileNumber())
                        .email(employee.getEmail())
                        .designation(employee.getDesignation())
                        .build()
        );
    }

    private Mono<Branch> getBranchData(String branchOid) {
        return userLoginPersistencePort.getBranchInformation(branchOid)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Branch not found with this Branch oid : " + branchOid)))
                .flatMap(branch -> {
                    if (branch.getStatus().equals("Active")) {
                        return Mono.just(branch);
                    } else {
                        log.error("User Branch is not currently active for login.");
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "User Branch : " + branch.getBranchName() + " is not currently active for login."));
                    }
                })
                .doOnNext(value -> log.info("Branch Data Response : {}", value))
                .doOnRequest(value -> log.info("Branch request for : {}", branchOid))
                .doOnSuccess(response -> log.info("Branch response is : {}", response))
                .doOnError(err -> log.info("Found error while get Branch data : {}", err.getLocalizedMessage()));
    }

    private Mono<Mfi> getMfiData(String mfiOid) {
        return userLoginPersistencePort.getMfiInformation(mfiOid)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Mfi not found with this Mfi oid : " + mfiOid)))
                .flatMap(mfi -> {
                    if (mfi.getStatus().equals("Active")) {
                        return Mono.just(mfi);
                    } else {
                        log.error("User Mfi is not currently active for login.");
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "User Mfi : " + mfi.getMfiName() + " is not currently active for login."));
                    }
                })
                .doOnNext(value -> log.info("Mfi Data Response : {}", value))
                .doOnRequest(value -> log.info("Mfi request for : {}", mfiOid))
                .doOnSuccess(response -> log.info("Mfi response is : {}", response))
                .doOnError(err -> log.info("Found error while get Mfi data : {}", err.getLocalizedMessage()));
    }

    private Mono<Employee> getEmployeeData(String userOid) {
        return userLoginPersistencePort.getEmployeeInformation(userOid)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Employee not found with this user oid : " + userOid)))
                .flatMap(employee -> {
                    if (employee.getStatus().equals("Active")) {
                        return Mono.just(employee);
                    } else {
                        log.error("Employee is not currently active for login.");
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Employee : " + employee.getFullName() + " is not currently active for login."));
                    }
                })
                .doOnNext(value -> log.info("Employee Data Response : {}", value))
                .doOnRequest(value -> log.info("Employee request for : {}", userOid))
                .doOnSuccess(response -> log.info("Employee response is : {}", response))
                .doOnError(err -> log.info("Found error while get Employee data : {}", err.getLocalizedMessage()));
    }

    private Mono<Role> getRoleData(String roleOid) {
        return userLoginPersistencePort.getRoleInformation(roleOid)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Role not found with this role oid : " + roleOid)))
                .flatMap(role -> {
                    if (role.getStatus().equals("Active")) {
                        return Mono.just(role);
                    } else {
                        log.error("User Role is not currently active for login.");
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "User Role : " + role.getRoleName() + " is not currently active for login."));
                    }
                })
                .doOnNext(value -> log.info("Role Data Response : {}", value))
                .doOnRequest(value -> log.info("Role request for : {}", roleOid))
                .doOnSuccess(response -> log.info("Role response is : {}", response))
                .doOnError(err -> log.info("Found error while get role data : {}", err.getLocalizedMessage()));
    }

    private Mono<UserRole> getUserRoleData(User user) {
        return userLoginPersistencePort.getUserRoleInformation(user.getOid())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "User Role not found with this user oid : " + user.getOid())))
                .doOnNext(value -> log.info("User Role Response is : {}", value))
                .doOnRequest(value -> log.info("User Role request for : {}", user.getOid()))
                .doOnSuccess(response -> log.info("User Role response from port : {}", response))
                .doOnError(err -> log.info("Found error while user role fetch from port : {}", err.getLocalizedMessage()));
    }

    private Mono<LoginTrail> saveLoginTrailData(UserRequestDto requestDto, User user) {
        return userLoginPersistencePort.saveLoginTrail(buildLoginTrail(requestDto, user))
                .doOnRequest(value -> log.info("Login Trail Save request for user : {}", user))
                .doOnSuccess(response -> log.info("Save Login Trail successfully : {}", response))
                .doOnError(err -> log.info("Found error while saving login trial data : {}", err.getLocalizedMessage()));
    }

    public boolean isCredentialsMatched(String rawPassword, String hashPassword) {
        return passwordEncoder.matches(rawPassword, hashPassword);
    }
}
