package net.akive.api.legal.application.provided;

import net.akive.api.legal.application.provided.dto.LegalDocumentActionStatusResult;
import net.akive.api.legal.domain.LegalDocumentActionContext;

import java.util.List;

public interface LegalDocumentActionFinder {

    List<LegalDocumentActionStatusResult> findAllCurrentByContext(
            Long userId,
            LegalDocumentActionContext actionContext
    );
}
