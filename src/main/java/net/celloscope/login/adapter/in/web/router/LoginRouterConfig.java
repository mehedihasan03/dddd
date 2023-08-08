package net.celloscope.login.adapter.in.web.router;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.login.adapter.in.web.handler.UserLoginHandler;
import net.celloscope.utils.RouteNames;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class LoginRouterConfig {

    private final UserLoginHandler userLoginHandler;

    @Bean
    public RouterFunction<ServerResponse> LoginRouterFunction() {
        return RouterFunctions.route()
                .POST(RouteNames.LOGIN, userLoginHandler::userLogin)
                .POST(RouteNames.LOGOUT, userLoginHandler::userLogOut)
                .before(this::loginRequestInputs)
                .after((req, res) -> logResponse(res))
                .build();
    }

    private ServerRequest loginRequestInputs(ServerRequest request) {
        log.info("Request Received: {}", request);
        return request;
    }

    private ServerResponse logResponse(ServerResponse response) {
        log.info("Response sent: {}", response);
        return response;
    }
}