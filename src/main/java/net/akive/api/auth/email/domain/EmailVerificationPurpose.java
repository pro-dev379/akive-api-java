package net.akive.api.auth.email.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailVerificationPurpose {

    JOIN("join"), FIND_PASSWORD("find-password");

    private final String code;
}
