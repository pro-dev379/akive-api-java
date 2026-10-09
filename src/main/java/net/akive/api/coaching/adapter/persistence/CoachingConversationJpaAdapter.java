package net.akive.api.coaching.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.coaching.adapter.persistence.entity.CoachingConversationEntity;
import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.domain.CoachingConversation;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class CoachingConversationJpaAdapter implements CoachingConversationRepository {

    private final CoachingConversationJpaRepository coachingConversationJpaRepository;

    @Override
    public CoachingConversation save(CoachingConversation coachingConversation) {
        return coachingConversationJpaRepository.save(CoachingConversationEntity.from(coachingConversation)).to();
    }
}
