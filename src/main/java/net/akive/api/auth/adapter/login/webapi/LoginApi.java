package net.akive.api.auth.adapter.login.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.adapter.login.webapi.dto.request.LoginByEmailRequest;
import net.akive.api.auth.adapter.login.webapi.dto.response.LoginResponse;
import net.akive.api.auth.application.login.provided.LoginProcessor;
import net.akive.api.auth.application.login.provided.dto.LoginByEmailCommand;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginApi {

    private final LoginProcessor loginProcessor;

    @PostMapping("/email")
    public LoginResponse loginByEmail(
            @RequestHeader("X-Client-Instance-Id") UUID clientInstanceIdHeader,
            @RequestBody @Validated LoginByEmailRequest request
    ) {
        LoginByEmailCommand command = LoginByEmailCommand.of(
                clientInstanceIdHeader,
                request.email(),
                request.password()
        );

        return LoginResponse.from(loginProcessor.loginByEmail(command));
    }
}
