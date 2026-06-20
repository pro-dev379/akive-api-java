package net.akive.api.user.application.dto;

import net.akive.api.user.domain.RoleType;

public record SignUpByEmailCommand(
        String email,
        RoleType roleType,
        String password
) {
}
