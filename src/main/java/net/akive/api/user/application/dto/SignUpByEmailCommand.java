package net.akive.api.user.application.dto;

import net.akive.api.user.domain.RoleType;

public record SignUpByEmailCommand(
        String email,
        RoleType roleType,
        String password
) {

    public static SignUpByEmailCommand of(String email, RoleType roleType, String password) {
        return new SignUpByEmailCommand(email, roleType, password);
    }
}
