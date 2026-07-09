package net.akive.api.user.application.dto;

import net.akive.api.common.domain.Email;
import net.akive.api.user.domain.RoleType;

public record SignUpByEmailCommand(
        Email email,
        RoleType roleType,
        String password
) {

    public static SignUpByEmailCommand of(String email, RoleType roleType, String password) {
        return new SignUpByEmailCommand(Email.of(email), roleType, password);
    }
}
