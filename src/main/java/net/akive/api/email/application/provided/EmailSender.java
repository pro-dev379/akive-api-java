package net.akive.api.email.application.provided;

import net.akive.api.email.application.dto.EmailSendCommand;

public interface EmailSender {

    void send(EmailSendCommand command);
}
