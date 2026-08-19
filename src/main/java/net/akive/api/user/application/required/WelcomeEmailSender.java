package net.akive.api.user.application.required;

import net.akive.api.common.domain.Email;

public interface WelcomeEmailSender {

    void send(Email email, String nickname);
}
