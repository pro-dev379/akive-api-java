package net.akive.api.legal.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.legal.application.provided.LegalDocumentActionFinder;
import net.akive.api.legal.application.provided.LegalDocumentFinder;
import net.akive.api.legal.application.provided.dto.LegalDocumentActionStatusResult;
import net.akive.api.legal.application.required.LegalDocumentActionRepository;
import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentAction;
import net.akive.api.legal.domain.LegalDocumentActionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class LegalDocumentActionQueryService implements LegalDocumentActionFinder {

    private final LegalDocumentFinder legalDocumentFinder;
    private final LegalDocumentActionRepository legalDocumentActionRepository;

    @Override
    public List<LegalDocumentActionStatusResult> findAllCurrentByContext(
            Long userId,
            LegalDocumentActionContext actionContext
    ) {
        List<LegalDocument> currentDocuments = legalDocumentFinder.findAllCurrentByContext(actionContext);
        Map<Long, LegalDocumentAction> latestActionByLegalDocumentId =
                findLatestActionByLegalDocumentId(userId, currentDocuments);

        return currentDocuments.stream()
                .map(document -> new LegalDocumentActionStatusResult(
                        document,
                        Optional.ofNullable(latestActionByLegalDocumentId.get(document.getLegalDocumentId()))
                ))
                .toList();
    }

    private Map<Long, LegalDocumentAction> findLatestActionByLegalDocumentId(
            Long userId,
            List<LegalDocument> currentDocuments
    ) {
        List<Long> legalDocumentIds = currentDocuments.stream()
                .map(LegalDocument::getLegalDocumentId)
                .toList();

        return legalDocumentActionRepository
                .findAllLatestByUserIdAndLegalDocumentIdIn(userId, legalDocumentIds)
                .stream()
                .collect(Collectors.toMap(
                        action -> action.getLegalDocument().getLegalDocumentId(),
                        Function.identity()
                ));
    }
}
