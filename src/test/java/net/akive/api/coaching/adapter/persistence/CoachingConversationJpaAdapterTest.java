package net.akive.api.coaching.adapter.persistence;

import net.akive.api.coaching.adapter.persistence.entity.CoachingConversationEntity;
import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static net.akive.api.coaching.domain.ConversationContext.MAIN;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.LLM_CONVERSATION_ID;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.createCoachingConversation;
import static org.assertj.core.api.Assertions.assertThat;

class CoachingConversationJpaAdapterTest extends RepositoryTestSupport {

    @Autowired
    CoachingConversationRepository coachingConversationRepository;

    @Test
    void 코칭_대화를_저장한다() {
        User savedUser = saveUser();
        CoachingConversation coachingConversation = createCoachingConversation(
                savedUser,
                LLM_CONVERSATION_ID,
                MAIN
        );

        CoachingConversation savedCoachingConversation = coachingConversationRepository.save(coachingConversation);
        flushAndClear();

        CoachingConversation foundCoachingConversation = entityManager
                .find(CoachingConversationEntity.class, savedCoachingConversation.getCoachingConversationId())
                .to();

        assertThat(foundCoachingConversation.getCoachingConversationId())
                .isEqualTo(savedCoachingConversation.getCoachingConversationId());
        assertThat(foundCoachingConversation.getUser().getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(foundCoachingConversation.getLlmConversationId()).isEqualTo(LLM_CONVERSATION_ID);
        assertThat(foundCoachingConversation.getTitle()).isNull();
        assertThat(foundCoachingConversation.getContext()).isEqualTo(MAIN);
    }
}
