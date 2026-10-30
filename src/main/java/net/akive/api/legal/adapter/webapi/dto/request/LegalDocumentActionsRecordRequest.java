package net.akive.api.legal.adapter.webapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import net.akive.api.legal.application.provided.dto.LegalDocumentActionsRecordCommand;
import net.akive.api.legal.domain.LegalDocumentActionContext;

import java.util.List;

public record LegalDocumentActionsRecordRequest(
        @NotNull LegalDocumentActionContext actionContext,
        @NotEmpty List<@NotNull @Valid LegalDocumentActionRecordRequest> actions
) {

    public LegalDocumentActionsRecordCommand to(Long userId) {
        return new LegalDocumentActionsRecordCommand(
                userId,
                this.actionContext,
                this.actions.stream().map(LegalDocumentActionRecordRequest::to).toList()
        );
    }
}
