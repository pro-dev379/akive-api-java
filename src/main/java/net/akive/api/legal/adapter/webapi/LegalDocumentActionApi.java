package net.akive.api.legal.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.legal.adapter.webapi.dto.request.LegalDocumentActionsRecordRequest;
import net.akive.api.legal.adapter.webapi.dto.response.LegalDocumentActionStatusesFindResponse;
import net.akive.api.legal.application.provided.LegalDocumentActionFinder;
import net.akive.api.legal.application.provided.LegalDocumentActionRecorder;
import net.akive.api.legal.domain.LegalDocumentActionContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/legal-documents")
@RequiredArgsConstructor
public class LegalDocumentActionApi {

    private final LegalDocumentActionFinder legalDocumentActionFinder;
    private final LegalDocumentActionRecorder legalDocumentActionRecorder;

    @GetMapping("/action-contexts/{actionContext}/current/action-statuses")
    public LegalDocumentActionStatusesFindResponse findAllCurrentStatuses(
            @AuthenticatedUserId Long userId,
            @PathVariable LegalDocumentActionContext actionContext
    ) {
        return LegalDocumentActionStatusesFindResponse.from(
                legalDocumentActionFinder.findAllCurrentByContext(userId, actionContext)
        );
    }

    @PostMapping("/action-contexts/{actionContext}/actions")
    @ResponseStatus(NO_CONTENT)
    public void record(
            @AuthenticatedUserId Long userId,
            @PathVariable LegalDocumentActionContext actionContext,
            @Validated @RequestBody LegalDocumentActionsRecordRequest request
    ) {
        legalDocumentActionRecorder.record(request.to(userId, actionContext));
    }
}
