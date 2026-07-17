package net.akive.api.user.presentation;

import lombok.RequiredArgsConstructor;
import net.akive.api.user.application.dto.SignUpResult;
import net.akive.api.user.application.port.in.SignUpByEmailUseCase;
import net.akive.api.user.presentation.dto.request.SignUpByEmailRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class SignUpController {

    private final SignUpByEmailUseCase signUpByEmailUseCase;

    @PostMapping("/email")
    @ResponseStatus(CREATED)
    public SignUpResult signUpByEmail(@Validated @RequestBody SignUpByEmailRequest request) {
        return signUpByEmailUseCase.signUp(request.to());
    }
}
