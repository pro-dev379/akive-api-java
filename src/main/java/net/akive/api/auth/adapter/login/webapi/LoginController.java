package net.akive.api.auth.adapter.login.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.adapter.login.webapi.dto.request.LoginByEmailRequest;
import net.akive.api.auth.application.login.provided.LoginByEmailUseCase;
import net.akive.api.auth.application.login.provided.dto.LoginByEmailCommand;
import net.akive.api.auth.application.login.provided.dto.LoginResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginByEmailUseCase loginByEmailUseCase;

    @PostMapping("/email")
    public LoginResult loginByEmail(
            @RequestHeader("X-Client-Instance-Id") UUID clientInstanceIdHeader,
            @RequestBody @Validated LoginByEmailRequest request
    ) {
        LoginByEmailCommand command = LoginByEmailCommand.of(
                clientInstanceIdHeader,
                request.email(),
                request.password()
        );

        return loginByEmailUseCase.login(command);
    }
}
