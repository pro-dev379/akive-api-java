package net.akive.api.coaching.application.required;

import net.akive.api.coaching.application.required.data.CoachingTurnRecoveryData;

import java.util.Optional;

public interface CoachingResponseRecoveryStore {

    Optional<CoachingTurnRecoveryData> find(Long coachingConversationId);

    void save(Long coachingConversationId, CoachingTurnRecoveryData recoveryData);

    void delete(Long coachingConversationId);
}
