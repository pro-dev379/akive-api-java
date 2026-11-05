package net.akive.api.legal.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.cache.ResponseCache;
import net.akive.api.legal.adapter.webapi.dto.response.LegalDocumentFindResponse;
import net.akive.api.legal.adapter.webapi.dto.response.LegalDocumentHeadersFindResponse;
import net.akive.api.legal.application.provided.LegalDocumentFinder;
import net.akive.api.legal.domain.LegalDocumentActionContext;
import net.akive.api.legal.domain.LegalDocumentType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.akive.api.common.cache.CacheKeyProvider.*;
import static net.akive.api.common.cache.ResponseCacheTtl.*;

@RestController
@RequestMapping("/legal-documents")
@RequiredArgsConstructor
public class LegalDocumentApi {

    private final LegalDocumentFinder legalDocumentFinder;
    private final ResponseCache responseCache;

    @GetMapping("/{legalDocumentId}")
    public LegalDocumentFindResponse find(@PathVariable Long legalDocumentId) {
        String cacheKey = legalDocument(legalDocumentId);

        return responseCache.get(cacheKey, LegalDocumentFindResponse.class)
                .orElseGet(() -> {
                    LegalDocumentFindResponse response =
                            LegalDocumentFindResponse.from(legalDocumentFinder.findOrThrow(legalDocumentId));
                    responseCache.put(cacheKey, response, LEGAL_DOCUMENT);

                    return response;
                });
    }

    @GetMapping("/types/{documentType}/history/headers")
    public LegalDocumentHeadersFindResponse findHistoryHeaders(@PathVariable LegalDocumentType documentType) {
        String cacheKey = legalDocumentHistoryHeaders(documentType);

        return responseCache.get(cacheKey, LegalDocumentHeadersFindResponse.class)
                .orElseGet(() -> {
                    LegalDocumentHeadersFindResponse response = LegalDocumentHeadersFindResponse.from(
                            legalDocumentFinder.findAllHistoryByDocumentType(documentType)
                    );
                    responseCache.put(cacheKey, response, LEGAL_DOCUMENT_HISTORY);

                    return response;
                });
    }

    @GetMapping("/types/{documentType}/current")
    public LegalDocumentFindResponse findCurrent(@PathVariable LegalDocumentType documentType) {
        String cacheKey = currentLegalDocument(documentType);

        return responseCache.get(cacheKey, LegalDocumentFindResponse.class)
                .orElseGet(() -> {
                    LegalDocumentFindResponse response = LegalDocumentFindResponse.from(
                            legalDocumentFinder.findCurrentByDocumentType(documentType)
                    );
                    responseCache.put(cacheKey, response, CURRENT_LEGAL_DOCUMENT);

                    return response;
                });
    }

    @GetMapping("/action-contexts/{actionContext}/current/headers")
    public LegalDocumentHeadersFindResponse findCurrentHeaders(
            @PathVariable LegalDocumentActionContext actionContext
    ) {
        String cacheKey = currentLegalDocumentHeaders(actionContext);

        return responseCache.get(cacheKey, LegalDocumentHeadersFindResponse.class)
                .orElseGet(() -> {
                    LegalDocumentHeadersFindResponse response = LegalDocumentHeadersFindResponse.from(
                            legalDocumentFinder.findAllCurrentByContext(actionContext)
                    );
                    responseCache.put(cacheKey, response, CURRENT_LEGAL_DOCUMENT_HEADERS);

                    return response;
                });
    }
}
