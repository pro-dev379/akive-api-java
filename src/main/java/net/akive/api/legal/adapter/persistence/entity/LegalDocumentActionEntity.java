package net.akive.api.legal.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import net.akive.api.legal.domain.LegalDocumentAction;
import net.akive.api.legal.domain.LegalDocumentActionContext;
import net.akive.api.legal.domain.LegalDocumentActionType;
import net.akive.api.user.adapter.persistence.entity.UserEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static org.hibernate.type.SqlTypes.BINARY;

@Entity
@Table(
        name = "legal_document_action",
        uniqueConstraints = @UniqueConstraint(
                name = "ux-legal_document_action-request_id",
                columnNames = "request_id"
        )
)
@Getter
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class LegalDocumentActionEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long legalDocumentActionId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserEntity user;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "legal_document_id", nullable = false, updatable = false)
    private LegalDocumentEntity legalDocument;

    @Enumerated(STRING)
    @Column(nullable = false, updatable = false, columnDefinition = "VARCHAR(50)")
    private LegalDocumentActionType actionType;

    @Enumerated(STRING)
    @Column(nullable = false, updatable = false, columnDefinition = "VARCHAR(50)")
    private LegalDocumentActionContext context;

    @JdbcTypeCode(BINARY)
    @Column(nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID requestId;

    @Column(nullable = false, updatable = false)
    private Instant actedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static LegalDocumentActionEntity from(LegalDocumentAction legalDocumentAction) {
        return new LegalDocumentActionEntity(
                legalDocumentAction.getLegalDocumentActionId(),
                UserEntity.from(legalDocumentAction.getUser()),
                LegalDocumentEntity.from(legalDocumentAction.getLegalDocument()),
                legalDocumentAction.getActionType(),
                legalDocumentAction.getActionContext(),
                legalDocumentAction.getRequestId(),
                legalDocumentAction.getActedAt(),
                legalDocumentAction.getCreatedAt()
        );
    }

    public LegalDocumentAction to() {
        try {
            return LegalDocumentAction.reconstruct(
                    this.legalDocumentActionId,
                    this.user.to(),
                    this.legalDocument.to(),
                    this.actionType,
                    this.context,
                    this.requestId,
                    this.actedAt,
                    this.createdAt
            );
        } catch (BusinessRuleViolationException e) {
            throw new InvalidPersistedDataException(
                    "Failed to reconstruct LegalDocumentAction: " + this.legalDocumentActionId,
                    e
            );
        }
    }
}
