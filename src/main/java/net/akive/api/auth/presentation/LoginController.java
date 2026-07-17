package net.akive.api.auth.presentation;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.result.login.LoginResult;
import net.akive.api.auth.application.port.in.login.LoginByEmailUseCase;
import net.akive.api.auth.presentation.dto.request.login.LoginByEmailRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginByEmailUseCase loginByEmailUseCase;

    @PostMapping("/email")
    public LoginResult loginByEmail(@RequestBody @Validated LoginByEmailRequest request) {
        return loginByEmailUseCase.login(request.to());
    }
}
