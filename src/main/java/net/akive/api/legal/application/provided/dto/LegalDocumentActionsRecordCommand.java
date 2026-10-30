package net.akive.api.legal.application.provided.dto;

import net.akive.api.common.domain.DomainValidator;
import net.akive.api.legal.domain.LegalDocumentActionContext;

import java.util.List;

import static net.akive.api.common.domain.DomainValidator.*;

public record LegalDocumentActionsRecordCommand(
        Long userId,
        LegalDocumentActionContext actionContext,
        List<LegalDocumentActionRecordItem> actionRecordItems
) {

    public LegalDocumentActionsRecordCommand {
        requirePositive(userId);
        requireNonNull(actionContext);
        requireNotEmpty(actionRecordItems).forEach(DomainValidator::requireNonNull);
        actionRecordItems = List.copyOf(actionRecordItems);
    }
}
