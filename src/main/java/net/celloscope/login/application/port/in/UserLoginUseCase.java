package net.celloscope.login.application.port.in;

import net.celloscope.login.adapter.in.dto.request.UserRequestDto;
import net.celloscope.login.adapter.in.dto.response.UserLoginResponseDto;
import reactor.core.publisher.Mono;

public interface UserLoginUseCase {

    Mono<UserLoginResponseDto> loginUser(UserRequestDto requestDto);
    Mono<String> logOutUser(String token, String ip);
}
