package net.akive.api.user.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.user.adapter.webapi.dto.request.UserSignUpByEmailRequest;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.dto.UserSignUpByEmailCommand;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberAccountPublicApi {

    private final UserAccountManager userAccountManager;

    @PostMapping("/email")
    @ResponseStatus(CREATED)
    public void signUpMemberByEmail(@Validated @RequestBody UserSignUpByEmailRequest request) {
        UserSignUpByEmailCommand command = UserSignUpByEmailCommand.of(
                request.email(),
                request.password(),
                request.nickname()
        );

        userAccountManager.signUpMemberByEmail(command);
    }
}
