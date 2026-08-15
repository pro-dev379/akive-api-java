package net.akive.api.user.application.provided.dto;

import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.common.domain.Email;
import net.akive.api.user.domain.RoleType;

import java.util.UUID;

public record SignUpByEmailCommand(
        ClientInstanceId clientInstanceId,
        Email email,
        RoleType roleType,
        String password
) {

    public static SignUpByEmailCommand of(
            UUID clientInstanceId,
            String email,
            RoleType roleType,
            String password
    ) {
        return new SignUpByEmailCommand(
                new ClientInstanceId(clientInstanceId),
                new Email(email),
                roleType,
                password
        );
    }
}
