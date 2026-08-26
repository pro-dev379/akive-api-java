package net.akive.api.mentoring.application.event;

import lombok.RequiredArgsConstructor;
import net.akive.api.mentoring.application.provided.MentoringProfileCreator;
import net.akive.api.user.application.event.UserSignedUpEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
class MentoringProfileCreationEventHandler {

    private final MentoringProfileCreator mentoringProfileCreator;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    void handle(UserSignedUpEvent userSignedUpEvent) {
        mentoringProfileCreator.create(userSignedUpEvent.userId());
    }
}
