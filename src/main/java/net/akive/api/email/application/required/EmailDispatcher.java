package net.akive.api.email.application.required;

import net.akive.api.email.domain.EmailMessage;

public interface EmailDispatcher {

    void dispatch(EmailMessage emailMessage);
}
