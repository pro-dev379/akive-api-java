package net.akive.api.auth.adapter.login.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.login.provided.dto.LoginResult;
import net.akive.api.auth.application.login.provided.LoginByEmailUseCase;
import net.akive.api.auth.adapter.login.webapi.dto.request.LoginByEmailRequest;
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
