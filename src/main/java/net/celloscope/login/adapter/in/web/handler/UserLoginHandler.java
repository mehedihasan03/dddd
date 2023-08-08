package net.celloscope.login.adapter.in.web.handler;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.login.adapter.in.dto.request.UserRequestDto;
import net.celloscope.login.application.port.in.UserLoginUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UserLoginHandler {

    private final UserLoginUseCase userLoginUseCase;

    public UserLoginHandler(UserLoginUseCase userLoginUseCase) {
        this.userLoginUseCase = userLoginUseCase;
    }

    public Mono<ServerResponse> userLogin(ServerRequest request) {
        String clientIp = request.remoteAddress()
                .map(address -> address.getAddress().getHostAddress())
                .orElse("Unknown IP");
        log.info("Client IP address: {}", clientIp);
        return request.bodyToMono(UserRequestDto.class)
                .flatMap(dto -> {
                    dto.setSourceIp(clientIp);
                    return userLoginUseCase.loginUser(dto);
                })
                .flatMap(responseDto -> ServerResponse.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDto))
                .switchIfEmpty(Mono.defer(() ->
                        ServerResponse.status(HttpStatus.BAD_REQUEST)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("Invalid Login Request")
                ))
                .doOnRequest(req -> log.info("Login Request for : {}", req))
                .doOnError(error -> log.error("Error occurred while login.", error))
                .onErrorResume(error -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(error.getMessage()));
    }

    public Mono<ServerResponse> userLogOut(ServerRequest request) {
        String clientIp = request.remoteAddress()
                .map(address -> address.getAddress().getHostAddress())
                .orElse("Unknown IP");
        log.info("Client IP address: {}", clientIp);
        String authToken = request.headers().firstHeader("auth-token");
        return userLoginUseCase.logOutUser(authToken, clientIp)
                .then(ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue("User logout successful."))
                .doOnRequest(req -> log.info("Logout Request for: {}", req))
                .doOnError(error -> log.error("Error occurred while logout.", error));
    }
}
