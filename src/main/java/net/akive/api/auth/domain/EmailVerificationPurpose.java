package net.akive.api.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailVerificationPurpose {

    SIGN_UP("sign-up"), FIND_PASSWORD("find-password");

    private final String code;
}
