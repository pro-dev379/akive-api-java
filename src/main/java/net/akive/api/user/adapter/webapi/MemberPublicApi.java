package net.akive.api.user.adapter.webapi;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import net.akive.api.user.adapter.webapi.dto.request.UserSignUpByEmailRequest;
import net.akive.api.user.adapter.webapi.dto.response.UserNicknameCheckResponse;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.UserNicknameChecker;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@Validated
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberPublicApi {

    private final UserAccountManager userAccountManager;
    private final UserNicknameChecker userNicknameChecker;

    @PostMapping("/email")
    @ResponseStatus(CREATED)
    public void signUpByEmail(@Validated @RequestBody UserSignUpByEmailRequest request) {
        userAccountManager.signUpMemberByEmail(request.to());
    }

    @GetMapping("/check-nickname")
    public UserNicknameCheckResponse checkNickname(@RequestParam @NotBlank String nickname) {
        boolean isDuplicated = userNicknameChecker.existsByNickname(nickname);

        return new UserNicknameCheckResponse(isDuplicated);
    }
}
