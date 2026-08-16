package net.akive.api.email.application.provided;

import net.akive.api.email.application.provided.dto.EmailSendCommand;

public interface EmailSender {

    void send(EmailSendCommand command);
}
