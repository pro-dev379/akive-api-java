package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.user.application.event.UserSignedUpEvent;
import net.akive.api.user.application.required.WelcomeEmailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
class WelcomeEmailEventHandler {

    private final WelcomeEmailSender welcomeEmailSender;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    void handle(UserSignedUpEvent userSignedUpEvent) {
        welcomeEmailSender.send(userSignedUpEvent.email(), userSignedUpEvent.nickname());
    }
}
