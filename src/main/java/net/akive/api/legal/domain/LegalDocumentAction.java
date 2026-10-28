package net.akive.api.legal.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.user.domain.User;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_LEGAL_DOCUMENT_ACTION_TYPE_FOR_INTERACTION_TYPE;
import static net.akive.api.legal.domain.LegalDocumentActionType.*;
import static net.akive.api.legal.domain.LegalInteractionType.*;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class LegalDocumentAction {

    private Long legalDocumentActionId;

    private User user;

    private LegalDocument legalDocument;

    private LegalDocumentActionType actionType;

    private LegalDocumentActionContext context;

    private UUID requestId;

    private Instant actedAt;

    private Instant createdAt;

    public static LegalDocumentAction reconstruct(
            Long legalDocumentActionId,
            User user,
            LegalDocument legalDocument,
            LegalDocumentActionType actionType,
            LegalDocumentActionContext actionContext,
            UUID requestId,
            Instant actedAt,
            Instant createdAt
    ) {
        LegalDocumentAction legalDocumentAction = LegalDocumentAction.builder()
                .legalDocumentActionId(requirePositive(legalDocumentActionId))
                .user(requireNonNull(user))
                .legalDocument(requireNonNull(legalDocument))
                .actionType(requireNonNull(actionType))
                .context(requireNonNull(actionContext))
                .requestId(requireNonNull(requestId))
                .actedAt(requireNonNull(actedAt))
                .createdAt(requireNonNull(createdAt))
                .build();

        legalDocumentAction.validateActionType();

        return legalDocumentAction;
    }

    public static LegalDocumentAction create(
            User user,
            LegalDocument legalDocument,
            LegalDocumentActionType actionType,
            LegalDocumentActionContext actionContext,
            UUID requestId,
            Instant actedAt
    ) {
        LegalDocumentAction legalDocumentAction = LegalDocumentAction.builder()
                .user(requireNonNull(user))
                .legalDocument(requireNonNull(legalDocument))
                .actionType(requireNonNull(actionType))
                .context(requireNonNull(actionContext))
                .requestId(requireNonNull(requestId))
                .actedAt(requireNonNull(actedAt))
                .build();

        legalDocumentAction.validateActionType();

        return legalDocumentAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LegalDocumentAction that = (LegalDocumentAction) o;

        return this.legalDocumentActionId != null
                && Objects.equals(this.legalDocumentActionId, that.legalDocumentActionId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private void validateActionType() {
        LegalInteractionType interactionType = this.legalDocument.getInteractionType();

        boolean valid = (interactionType == AGREEMENT && this.actionType == AGREED)
                || (interactionType == ACKNOWLEDGEMENT && this.actionType == ACKNOWLEDGED)
                || (interactionType == CONFIRMATION && this.actionType == CONFIRMED)
                || (interactionType == CONSENT
                && (this.actionType == AGREED || this.actionType == DECLINED || this.actionType == REVOKED));

        if (!valid) {
            throw new BusinessRuleViolationException(INVALID_LEGAL_DOCUMENT_ACTION_TYPE_FOR_INTERACTION_TYPE);
        }
    }
}
