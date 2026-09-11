package net.akive.api.auth.adapter.password.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.adapter.password.webapi.dto.PasswordUpdateRequest;
import net.akive.api.auth.application.password.provided.PasswordModifier;
import net.akive.api.core.config.security.AuthenticatedUserId;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PasswordApi {

    private final PasswordModifier passwordModifier;

    @PatchMapping("/me/password")
    @ResponseStatus(NO_CONTENT)
    public void update(@AuthenticatedUserId Long userId, @Validated @RequestBody PasswordUpdateRequest request) {
        passwordModifier.update(request.to(userId));
    }
}
