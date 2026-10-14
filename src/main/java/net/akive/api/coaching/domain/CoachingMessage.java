package net.akive.api.coaching.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;

import java.time.Instant;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.coaching.domain.CoachingPolicy.USER_MESSAGE_CONTENT_MAX_LENGTH;
import static net.akive.api.coaching.domain.MessageCompletionStatus.COMPLETED;
import static net.akive.api.coaching.domain.MessageRole.ASSISTANT;
import static net.akive.api.coaching.domain.MessageRole.USER;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_COACHING_MESSAGE_COMPLETION_STATUS;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class CoachingMessage {

    private Long coachingMessageId;

    private CoachingConversation coachingConversation;

    private MessageRole role;

    private String content;

    private MessageCompletionStatus completionStatus;

    private Instant createdAt;

    public static CoachingMessage reconstruct(
            Long coachingMessageId,
            CoachingConversation coachingConversation,
            MessageRole role,
            String content,
            MessageCompletionStatus completionStatus,
            Instant createdAt
    ) {
        return CoachingMessage.builder()
                .coachingMessageId(requirePositive(coachingMessageId))
                .coachingConversation(requireNonNull(coachingConversation))
                .role(requireNonNull(role))
                .content(validateContent(role, content))
                .completionStatus(validateCompletionStatus(role, completionStatus))
                .createdAt(requireNonNull(createdAt))
                .build();
    }

    static CoachingMessage create(
            CoachingConversation coachingConversation,
            MessageRole role,
            String content,
            MessageCompletionStatus completionStatus
    ) {
        return CoachingMessage.builder()
                .coachingConversation(requireNonNull(coachingConversation))
                .role(requireNonNull(role))
                .content(validateContent(role, content))
                .completionStatus(validateCompletionStatus(role, completionStatus))
                .build();
    }

    public static CoachingMessage fromUser(CoachingConversation coachingConversation, String content) {
        return create(coachingConversation, USER, content, COMPLETED);
    }

    public static CoachingMessage fromAssistant(
            CoachingConversation coachingConversation,
            String content,
            MessageCompletionStatus completionStatus
    ) {
        return create(coachingConversation, ASSISTANT, content, completionStatus);
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

    private static String validateContent(MessageRole role, String content) {
        String validatedContent = requireNotBlank(content);

        if (role == USER) {
            return requireMaxLength(validatedContent, USER_MESSAGE_CONTENT_MAX_LENGTH);
        }

        return validatedContent;
    }

    private static MessageCompletionStatus validateCompletionStatus(
            MessageRole role,
            MessageCompletionStatus completionStatus
    ) {
        MessageCompletionStatus validatedCompletionStatus = requireNonNull(completionStatus);

        if (role == USER && validatedCompletionStatus != COMPLETED) {
            throw new BusinessRuleViolationException(INVALID_COACHING_MESSAGE_COMPLETION_STATUS);
        }

        return validatedCompletionStatus;
    }
}
