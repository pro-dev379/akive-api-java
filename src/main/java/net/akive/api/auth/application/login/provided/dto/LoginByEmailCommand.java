package net.akive.api.auth.application.login.provided.dto;

import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.common.domain.Email;

import java.util.UUID;

public record LoginByEmailCommand(ClientInstanceId clientInstanceId, Email email, String password) {

    public static LoginByEmailCommand of(UUID clientInstanceId, String email, String password) {
        return new LoginByEmailCommand(new ClientInstanceId(clientInstanceId), new Email(email), password);
    }
}
