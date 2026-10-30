package net.akive.api.legal.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.legal.application.provided.LegalDocumentActionFinder;
import net.akive.api.legal.application.required.LegalDocumentActionRepository;
import net.akive.api.legal.domain.LegalDocumentAction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.LEGAL_DOCUMENT_ACTION_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class LegalDocumentActionQueryService implements LegalDocumentActionFinder {

    private final LegalDocumentActionRepository legalDocumentActionRepository;

    @Override
    public LegalDocumentAction findCurrentOrThrow(Long userId, Long legalDocumentId)
            throws ResourceNotFoundException {
        return legalDocumentActionRepository.findLatestByUserIdAndLegalDocumentId(userId, legalDocumentId)
                .orElseThrow(() -> new ResourceNotFoundException(LEGAL_DOCUMENT_ACTION_NOT_FOUND));
    }
}
