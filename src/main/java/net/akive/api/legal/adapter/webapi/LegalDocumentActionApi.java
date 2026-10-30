package net.akive.api.legal.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.legal.adapter.webapi.dto.request.LegalDocumentActionsRecordRequest;
import net.akive.api.legal.adapter.webapi.dto.response.LegalDocumentActionFindResponse;
import net.akive.api.legal.application.provided.LegalDocumentActionFinder;
import net.akive.api.legal.application.provided.LegalDocumentActionRecorder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/legal-documents")
@RequiredArgsConstructor
public class LegalDocumentActionApi {

    private final LegalDocumentActionFinder legalDocumentActionFinder;
    private final LegalDocumentActionRecorder legalDocumentActionRecorder;

    @GetMapping("/{legalDocumentId}/actions/current")
    public LegalDocumentActionFindResponse findCurrent(
            @AuthenticatedUserId Long userId,
            @PathVariable Long legalDocumentId
    ) {
        return LegalDocumentActionFindResponse.from(
                legalDocumentActionFinder.findCurrentOrThrow(userId, legalDocumentId)
        );
    }

    @PostMapping("/actions")
    @ResponseStatus(NO_CONTENT)
    public void record(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody LegalDocumentActionsRecordRequest request
    ) {
        legalDocumentActionRecorder.record(request.to(userId));
    }
}
