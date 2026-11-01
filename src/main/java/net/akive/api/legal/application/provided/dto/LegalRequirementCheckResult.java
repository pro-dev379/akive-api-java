package net.akive.api.legal.application.provided.dto;

import net.akive.api.legal.domain.LegalDocumentType;

import java.util.Set;

public record LegalRequirementCheckResult(boolean satisfied, Set<LegalDocumentType> unsatisfiedDocumentTypes) {

    public LegalRequirementCheckResult {
        unsatisfiedDocumentTypes = Set.copyOf(unsatisfiedDocumentTypes);
    }

    public static LegalRequirementCheckResult allSatisfied() {
        return new LegalRequirementCheckResult(true, Set.of());
    }

    public static LegalRequirementCheckResult withUnsatisfiedDocuments(
            Set<LegalDocumentType> unsatisfiedDocumentTypes
    ) {
        return new LegalRequirementCheckResult(false, unsatisfiedDocumentTypes);
    }
}
