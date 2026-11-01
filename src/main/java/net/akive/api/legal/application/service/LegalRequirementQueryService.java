package net.akive.api.legal.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.legal.application.provided.LegalDocumentFinder;
import net.akive.api.legal.application.provided.LegalRequirementChecker;
import net.akive.api.legal.application.provided.dto.LegalRequirementCheckResult;
import net.akive.api.legal.application.required.LegalDocumentActionRepository;
import net.akive.api.legal.domain.LegalDocumentActionType;
import net.akive.api.legal.domain.LegalDocumentType;
import net.akive.api.legal.domain.LegalPolicy;
import net.akive.api.legal.domain.LegalRequirementScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class LegalRequirementQueryService implements LegalRequirementChecker {

    private final LegalDocumentFinder legalDocumentFinder;
    private final LegalDocumentActionRepository legalDocumentActionRepository;

    @Override
    public LegalRequirementCheckResult check(Long userId, LegalRequirementScope scope) {
        Map<LegalDocumentType, LegalDocumentActionType> requirements = LegalPolicy.requirementsFor(scope);
        Set<LegalDocumentType> unsatisfiedDocumentTypes = EnumSet.noneOf(LegalDocumentType.class);

        for (Map.Entry<LegalDocumentType, LegalDocumentActionType> requirement : requirements.entrySet()) {
            Long legalDocumentId = findCurrentDocumentIdOrThrow(requirement.getKey());

            boolean satisfied = legalDocumentActionRepository
                    .findLatestByUserIdAndLegalDocumentId(userId, legalDocumentId)
                    .map(action -> action.getActionType() == requirement.getValue())
                    .orElse(false);

            if (!satisfied) {
                unsatisfiedDocumentTypes.add(requirement.getKey());
            }
        }

        return unsatisfiedDocumentTypes.isEmpty()
                ? LegalRequirementCheckResult.allSatisfied()
                : LegalRequirementCheckResult.withUnsatisfiedDocuments(unsatisfiedDocumentTypes);
    }

    private Long findCurrentDocumentIdOrThrow(LegalDocumentType documentType) {
        try {
            return legalDocumentFinder.findCurrentByDocumentType(documentType).getLegalDocumentId();
        } catch (ResourceNotFoundException e) {
            throw new IllegalStateException(
                    "Current legal document required for service access is not configured: documentType="
                            + documentType,
                    e
            );
        }
    }
}
