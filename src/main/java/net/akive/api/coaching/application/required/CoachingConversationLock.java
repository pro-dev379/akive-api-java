package net.akive.api.coaching.application.required;

import java.util.Optional;

public interface CoachingConversationLock {

    Optional<Lease> tryAcquire(Long coachingConversationId);

    interface Lease {

        void release();
    }
}
