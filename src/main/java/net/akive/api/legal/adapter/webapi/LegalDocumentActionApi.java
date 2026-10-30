package net.akive.api.legal.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.legal.adapter.webapi.dto.response.LegalDocumentActionFindResponse;
import net.akive.api.legal.application.provided.LegalDocumentActionFinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/legal-documents/{legalDocumentId}/legal-document-actions")
@RequiredArgsConstructor
public class LegalDocumentActionApi {

    private final LegalDocumentActionFinder legalDocumentActionFinder;

    @GetMapping("/current")
    public LegalDocumentActionFindResponse findCurrent(
            @AuthenticatedUserId Long userId,
            @PathVariable Long legalDocumentId
    ) {
        return LegalDocumentActionFindResponse.from(
                legalDocumentActionFinder.findCurrentOrThrow(userId, legalDocumentId)
        );
    }
}
