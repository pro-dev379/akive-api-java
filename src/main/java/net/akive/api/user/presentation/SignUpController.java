package net.akive.api.user.presentation;

import lombok.RequiredArgsConstructor;
import net.akive.api.user.application.dto.SignUpResult;
import net.akive.api.user.application.port.in.SignUpByEmailUseCase;
import net.akive.api.user.presentation.dto.request.SignUpByEmailRequest;
import net.akive.api.user.presentation.dto.response.SignUpResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class SignUpController {

    private final SignUpByEmailUseCase signUpByEmailUseCase;

    @PostMapping("/email")
    public SignUpResponse signUpByEmail(@Validated @RequestBody SignUpByEmailRequest request) {
        SignUpResult signUpResult = signUpByEmailUseCase.signUp(request.toCommand());
        return SignUpResponse.from(signUpResult);
    }
}
