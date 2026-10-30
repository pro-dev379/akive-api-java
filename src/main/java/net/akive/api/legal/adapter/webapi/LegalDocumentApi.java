package net.akive.api.legal.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.legal.adapter.webapi.dto.response.LegalDocumentFindResponse;
import net.akive.api.legal.adapter.webapi.dto.response.LegalDocumentHeadersFindResponse;
import net.akive.api.legal.adapter.webapi.dto.response.LegalDocumentIdFindResponse;
import net.akive.api.legal.application.provided.LegalDocumentFinder;
import net.akive.api.legal.domain.LegalDocumentActionContext;
import net.akive.api.legal.domain.LegalDocumentType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/legal-documents")
@RequiredArgsConstructor
public class LegalDocumentApi {

    private final LegalDocumentFinder legalDocumentFinder;

    @GetMapping("/{legalDocumentId}")
    public LegalDocumentFindResponse find(@PathVariable Long legalDocumentId) {
        return LegalDocumentFindResponse.from(legalDocumentFinder.findOrThrow(legalDocumentId));
    }

    @GetMapping("/types/{documentType}/current/id")
    public LegalDocumentIdFindResponse findCurrentId(@PathVariable LegalDocumentType documentType) {
        return new LegalDocumentIdFindResponse(legalDocumentFinder.findCurrentIdByDocumentType(documentType));
    }

    @GetMapping("/action-contexts/{actionContext}/current/headers")
    public LegalDocumentHeadersFindResponse findAllCurrentHeaders(
            @PathVariable LegalDocumentActionContext actionContext
    ) {
        return LegalDocumentHeadersFindResponse.from(
                legalDocumentFinder.findAllCurrentByContext(actionContext)
        );
    }
}
