package net.akive.api.user.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.user.application.provided.dto.SignUpByEmailCommand;
import net.akive.api.user.application.provided.dto.SignUpResult;
import net.akive.api.user.application.provided.SignUpByEmailUseCase;
import net.akive.api.user.adapter.webapi.dto.request.SignUpByEmailRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class SignUpController {

    private final SignUpByEmailUseCase signUpByEmailUseCase;

    @PostMapping("/email")
    @ResponseStatus(CREATED)
    public SignUpResult signUpByEmail(
            @RequestHeader("X-Client-Instance-Id") UUID ClientInstanceIdHeader,
            @Validated @RequestBody SignUpByEmailRequest request
    ) {
        return signUpByEmailUseCase.signUp(
                SignUpByEmailCommand.of(
                        ClientInstanceIdHeader,
                        request.email(),
                        request.roleType(),
                        request.password()
                )
        );
    }
}
