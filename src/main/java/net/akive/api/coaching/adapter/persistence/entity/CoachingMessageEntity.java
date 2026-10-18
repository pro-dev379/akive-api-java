package net.akive.api.coaching.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.coaching.domain.CoachingMessage;
import net.akive.api.coaching.domain.MessageCompletionStatus;
import net.akive.api.coaching.domain.MessageRole;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "coaching_message")
@Getter
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class CoachingMessageEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long coachingMessageId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "coaching_conversation_id", nullable = false)
    private CoachingConversationEntity coachingConversation;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private MessageRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private MessageCompletionStatus completionStatus;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static CoachingMessageEntity from(CoachingMessage coachingMessage) {
        return new CoachingMessageEntity(
                coachingMessage.getCoachingMessageId(),
                CoachingConversationEntity.from(coachingMessage.getCoachingConversation()),
                coachingMessage.getRole(),
                coachingMessage.getContent(),
                coachingMessage.getCompletionStatus(),
                coachingMessage.getCreatedAt()
        );
    }

    public CoachingMessage to() {
        try {
            return CoachingMessage.reconstruct(
                    this.coachingMessageId,
                    this.coachingConversation.to(),
                    this.role,
                    this.content,
                    this.completionStatus,
                    this.createdAt
            );
        } catch (BusinessRuleViolationException e) {
            throw new InvalidPersistedDataException(
                    "Failed to reconstruct CoachingMessage: " + this.coachingMessageId,
                    e
            );
        }
    }
}
