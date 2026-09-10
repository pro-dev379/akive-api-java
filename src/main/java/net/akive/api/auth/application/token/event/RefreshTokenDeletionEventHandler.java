package net.akive.api.auth.application.token.event;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.token.provided.RefreshTokenSessionManager;
import net.akive.api.user.application.event.UserWithdrawnEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
class RefreshTokenDeletionEventHandler {

    private final RefreshTokenSessionManager refreshTokenSessionManager;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    void handle(UserWithdrawnEvent event) {
        refreshTokenSessionManager.revokeByUserId(event.userId());
    }
}
