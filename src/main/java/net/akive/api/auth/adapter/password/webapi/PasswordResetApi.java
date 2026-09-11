package net.akive.api.auth.adapter.password.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.adapter.password.webapi.dto.PasswordResetRequest;
import net.akive.api.auth.application.password.provided.PasswordModifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetApi {

    private final PasswordModifier passwordModifier;

    @PatchMapping
    @ResponseStatus(NO_CONTENT)
    public void reset(@Validated @RequestBody PasswordResetRequest request) {
        passwordModifier.reset(request.to());
    }
}
