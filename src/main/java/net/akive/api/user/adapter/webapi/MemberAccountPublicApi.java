package net.akive.api.user.adapter.webapi;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import net.akive.api.user.adapter.webapi.dto.request.UserSignUpByEmailRequest;
import net.akive.api.user.adapter.webapi.dto.response.UserNicknameCheckResponse;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.UserNicknameChecker;
import net.akive.api.user.application.provided.dto.UserSignUpByEmailCommand;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@Validated
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberAccountPublicApi {

    private final UserNicknameChecker userNicknameChecker;
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

    @GetMapping("/check-nickname")
    public UserNicknameCheckResponse checkNickname(@RequestParam @NotBlank String nickname) {
        boolean isDuplicated = userNicknameChecker.existsByNickname(nickname);

        return new UserNicknameCheckResponse(isDuplicated);
    }
}
