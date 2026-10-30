package net.akive.api.legal.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.legal.application.provided.LegalDocumentActionRecorder;
import net.akive.api.legal.application.provided.LegalDocumentFinder;
import net.akive.api.legal.application.provided.dto.LegalDocumentActionRecordItem;
import net.akive.api.legal.application.provided.dto.LegalDocumentActionsRecordCommand;
import net.akive.api.legal.application.required.LegalDocumentActionRepository;
import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentAction;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.common.exception.business.ResourceConflictError.LEGAL_DOCUMENT_ACTION_REQUEST_CONFLICT;
import static net.akive.api.legal.domain.LegalDocumentActionContext.POLICY_UPDATE;
import static net.akive.api.legal.domain.LegalDocumentActionContext.SETTINGS;
import static net.akive.api.legal.domain.LegalInteractionType.CONSENT;

@Service
@Transactional
@RequiredArgsConstructor
class LegalDocumentActionCommandService implements LegalDocumentActionRecorder {

    private final UserFinder userFinder;
    private final LegalDocumentFinder legalDocumentFinder;

    private final LegalDocumentActionRepository legalDocumentActionRepository;

    private final CurrentTimeProvider currentTimeProvider;

    @Override
    public void record(LegalDocumentActionsRecordCommand command)
            throws BusinessRuleViolationException, ResourceConflictException, ResourceNotFoundException {
        validateActionRecordItemsNotDuplicated(command.actionRecordItems());

        User user = userFinder.findOrThrow(command.userId());
        Map<UUID, LegalDocumentAction> existingActionByRequestId = findExistingActionByRequestId(command);

        if (existingActionByRequestId.size() == command.actionRecordItems().size()) {
            return;
        }

        Map<Long, LegalDocument> requestedDocumentById = findRequestedDocumentById(command);
        List<LegalDocument> currentDocuments = findCurrentDocuments(command, requestedDocumentById.values());

        validateActionsForContext(requestedDocumentById, currentDocuments);

        Instant actedAt = currentTimeProvider.now();

        for (LegalDocumentActionRecordItem actionItem : command.actionRecordItems()) {
            if (existingActionByRequestId.containsKey(actionItem.requestId())) {
                continue;
            }

            LegalDocument legalDocument = requestedDocumentById.get(actionItem.legalDocumentId());
            legalDocumentActionRepository.save(LegalDocumentAction.create(
                    user,
                    legalDocument,
                    actionItem.actionType(),
                    command.actionContext(),
                    actionItem.requestId(),
                    actedAt
            ));
        }
    }

    private Map<UUID, LegalDocumentAction> findExistingActionByRequestId(
            LegalDocumentActionsRecordCommand command
    ) {
        Map<UUID, LegalDocumentAction> existingActionByRequestId = new HashMap<>();

        for (LegalDocumentActionRecordItem actionItem : command.actionRecordItems()) {
            legalDocumentActionRepository.findByRequestId(actionItem.requestId())
                    .ifPresent(existingAction -> {
                        validateDuplicateRequestConsistency(existingAction, command, actionItem);
                        existingActionByRequestId.put(actionItem.requestId(), existingAction);
                    });
        }

        return existingActionByRequestId;
    }

    private void validateActionRecordItemsNotDuplicated(
            List<LegalDocumentActionRecordItem> actionRecordItems
    ) {
        Set<Long> legalDocumentIds = new HashSet<>();
        Set<UUID> requestIds = new HashSet<>();

        for (LegalDocumentActionRecordItem actionItem : actionRecordItems) {
            if (!legalDocumentIds.add(actionItem.legalDocumentId()) || !requestIds.add(actionItem.requestId())) {
                throw new BusinessRuleViolationException(DUPLICATE_LEGAL_DOCUMENT_ACTION_REQUEST);
            }
        }
    }

    private Map<Long, LegalDocument> findRequestedDocumentById(LegalDocumentActionsRecordCommand command) {
        return command.actionRecordItems().stream()
                .map(LegalDocumentActionRecordItem::legalDocumentId)
                .map(legalDocumentFinder::findOrThrow)
                .collect(Collectors.toMap(
                        LegalDocument::getLegalDocumentId,
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }

    private List<LegalDocument> findCurrentDocuments(
            LegalDocumentActionsRecordCommand command,
            Collection<LegalDocument> requestedDocuments
    ) {
        if (command.actionContext() != SETTINGS && command.actionContext() != POLICY_UPDATE) {
            return legalDocumentFinder.findAllCurrentByContext(command.actionContext());
        }

        for (LegalDocument legalDocument : requestedDocuments) {
            Long currentLegalDocumentId =
                    legalDocumentFinder.findCurrentIdByDocumentType(legalDocument.getDocumentType());

            if (!legalDocument.getLegalDocumentId().equals(currentLegalDocumentId)) {
                throw new BusinessRuleViolationException(INVALID_LEGAL_DOCUMENT_ACTION_FOR_CONTEXT);
            }

            if (command.actionContext() == SETTINGS && legalDocument.getInteractionType() != CONSENT) {
                throw new BusinessRuleViolationException(INVALID_LEGAL_DOCUMENT_ACTION_FOR_CONTEXT);
            }
        }

        return List.copyOf(requestedDocuments);
    }

    private void validateActionsForContext(
            Map<Long, LegalDocument> requestedDocumentById,
            List<LegalDocument> currentDocuments
    ) {
        Set<Long> currentDocumentIds = currentDocuments.stream()
                .map(LegalDocument::getLegalDocumentId)
                .collect(Collectors.toSet());

        if (!currentDocumentIds.containsAll(requestedDocumentById.keySet())) {
            throw new BusinessRuleViolationException(INVALID_LEGAL_DOCUMENT_ACTION_FOR_CONTEXT);
        }

        boolean requiredDocumentMissing = currentDocuments.stream()
                .filter(legalDocument -> !legalDocument.isOptional())
                .map(LegalDocument::getLegalDocumentId)
                .anyMatch(legalDocumentId -> !requestedDocumentById.containsKey(legalDocumentId));

        if (requiredDocumentMissing) {
            throw new BusinessRuleViolationException(REQUIRED_LEGAL_DOCUMENT_ACTION_MISSING);
        }
    }

    private void validateDuplicateRequestConsistency(
            LegalDocumentAction existingAction,
            LegalDocumentActionsRecordCommand command,
            LegalDocumentActionRecordItem actionItem
    ) {
        boolean consistent = existingAction.getUser().getUserId().equals(command.userId())
                && existingAction.getLegalDocument().getLegalDocumentId().equals(actionItem.legalDocumentId())
                && existingAction.getActionType() == actionItem.actionType()
                && existingAction.getContext() == command.actionContext();

        if (!consistent) {
            throw new ResourceConflictException(LEGAL_DOCUMENT_ACTION_REQUEST_CONFLICT);
        }
    }
}
