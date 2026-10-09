package net.akive.api.coaching.application.required;

import net.akive.api.coaching.domain.CoachingConversation;

public interface CoachingConversationRepository {

    CoachingConversation save(CoachingConversation coachingConversation);
}
