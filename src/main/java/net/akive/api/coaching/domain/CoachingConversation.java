package net.akive.api.coaching.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.user.domain.User;

import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class CoachingConversation {

    private Long coachingConversationId;

    private User user;

    private String llmConversationId;

    private String title;

    private ConversationContext context;

    public static CoachingConversation reconstruct(
            Long coachingConversationId,
            User user,
            String llmConversationId,
            String title,
            ConversationContext context
    ) {
        return CoachingConversation.builder()
                .coachingConversationId(requirePositive(coachingConversationId))
                .user(requireNonNull(user))
                .llmConversationId(requireNotBlank(llmConversationId))
                .title(title == null ? null : requireNotBlank(title))
                .context(requireNonNull(context))
                .build();
    }

    public static CoachingConversation create(
            User user,
            String llmConversationId,
            ConversationContext context
    ) {
        return CoachingConversation.builder()
                .user(requireNonNull(user))
                .llmConversationId(requireNotBlank(llmConversationId))
                .context(requireNonNull(context))
                .build();
    }

    public void updateTitle(String title) {
        this.title = requireNotBlank(title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoachingConversation that = (CoachingConversation) o;

        return this.coachingConversationId != null
                && Objects.equals(this.coachingConversationId, that.coachingConversationId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
