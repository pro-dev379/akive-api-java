package net.akive.api.legal.adapter.user;

import lombok.RequiredArgsConstructor;
import net.akive.api.legal.application.provided.LegalDocumentActionRecorder;
import net.akive.api.legal.application.provided.dto.LegalDocumentActionRecordItem;
import net.akive.api.legal.application.provided.dto.LegalDocumentActionsRecordCommand;
import net.akive.api.legal.domain.LegalDocumentActionType;
import net.akive.api.user.domain.SignUpLegalDocumentActionType;
import net.akive.api.user.application.required.SignUpLegalDocumentActionRecorder;
import net.akive.api.user.application.required.data.SignUpLegalDocumentActionData;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.akive.api.legal.domain.LegalDocumentActionContext.SIGN_UP;

@Component
@RequiredArgsConstructor
class SignUpLegalDocumentActionRecorderAdapter implements SignUpLegalDocumentActionRecorder {

    private final LegalDocumentActionRecorder legalDocumentActionRecorder;

    @Override
    public void record(Long userId, List<SignUpLegalDocumentActionData> legalDocumentActions) {
        List<LegalDocumentActionRecordItem> actionRecordItems = legalDocumentActions.stream()
                .map(action -> new LegalDocumentActionRecordItem(
                        action.requestId(),
                        action.legalDocumentId(),
                        toLegalDocumentActionType(action.actionType())
                ))
                .toList();

        legalDocumentActionRecorder.record(new LegalDocumentActionsRecordCommand(
                userId,
                SIGN_UP,
                actionRecordItems
        ));
    }

    private LegalDocumentActionType toLegalDocumentActionType(SignUpLegalDocumentActionType actionType) {
        return switch (actionType) {
            case AGREED -> LegalDocumentActionType.AGREED;
            case DECLINED -> LegalDocumentActionType.DECLINED;
            case ACKNOWLEDGED -> LegalDocumentActionType.ACKNOWLEDGED;
            case CONFIRMED -> LegalDocumentActionType.CONFIRMED;
        };
    }
}
