package net.akive.api.auth.domain.emailverification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailVerificationPurpose {

    SIGN_UP("sign-up"), PASSWORD_RESET("password-reset");

    private final String code;
}
