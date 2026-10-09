package net.akive.api.coaching.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class CoachingMessage {

    private Long coachingMessageId;

    private CoachingConversation coachingConversation;

    private MessageRole role;

    private String content;

    private Instant createdAt;

    public static CoachingMessage reconstruct(
            Long coachingMessageId,
            CoachingConversation coachingConversation,
            MessageRole role,
            String content,
            Instant createdAt
    ) {
        return CoachingMessage.builder()
                .coachingMessageId(requirePositive(coachingMessageId))
                .coachingConversation(requireNonNull(coachingConversation))
                .role(requireNonNull(role))
                .content(requireNotBlank(content))
                .createdAt(requireNonNull(createdAt))
                .build();
    }

    static CoachingMessage create(
            CoachingConversation coachingConversation,
            MessageRole role,
            String content
    ) {
        return CoachingMessage.builder()
                .coachingConversation(requireNonNull(coachingConversation))
                .role(requireNonNull(role))
                .content(requireNotBlank(content))
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoachingMessage that = (CoachingMessage) o;

        return this.coachingMessageId != null && Objects.equals(this.coachingMessageId, that.coachingMessageId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
