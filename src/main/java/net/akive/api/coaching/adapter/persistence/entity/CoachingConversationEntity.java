package net.akive.api.coaching.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.coaching.domain.ConversationContext;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.user.adapter.persistence.entity.UserEntity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
        name = "coaching_conversation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-coaching_conversation-llm_conversation_id",
                        columnNames = "llm_conversation_id"
                )
        }
)
@Getter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class CoachingConversationEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long coachingConversationId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String llmConversationId;

    private String title;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private ConversationContext context;

    public static CoachingConversationEntity from(CoachingConversation coachingConversation) {
        CoachingConversationEntity coachingConversationEntity = new CoachingConversationEntity(
                coachingConversation.getCoachingConversationId(),
                UserEntity.from(coachingConversation.getUser()),
                coachingConversation.getLlmConversationId(),
                coachingConversation.getTitle(),
                coachingConversation.getContext()
        );

        coachingConversationEntity.restoreAuditTime(
                coachingConversation.getCreatedAt(),
                coachingConversation.getUpdatedAt()
        );

        return coachingConversationEntity;
    }

    public CoachingConversation to() {
        try {
            return CoachingConversation.reconstruct(
                    this.coachingConversationId,
                    this.user.to(),
                    this.llmConversationId,
                    this.title,
                    this.context,
                    this.getCreatedAt(),
                    this.getUpdatedAt()
            );
        } catch (BusinessRuleViolationException e) {
            throw new InvalidPersistedDataException(
                    "Failed to reconstruct CoachingConversation: " + this.coachingConversationId,
                    e
            );
        }
    }
}
