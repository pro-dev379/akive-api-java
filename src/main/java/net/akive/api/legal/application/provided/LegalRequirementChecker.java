package net.akive.api.legal.application.provided;

import net.akive.api.legal.application.provided.dto.LegalRequirementCheckResult;
import net.akive.api.legal.domain.LegalRequirementScope;

public interface LegalRequirementChecker {

    LegalRequirementCheckResult check(Long userId, LegalRequirementScope scope);
}
