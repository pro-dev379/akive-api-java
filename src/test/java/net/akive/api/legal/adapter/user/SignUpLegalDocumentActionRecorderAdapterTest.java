package net.akive.api.legal.adapter.user;

import net.akive.api.legal.application.provided.LegalDocumentActionRecorder;
import net.akive.api.legal.application.provided.dto.LegalDocumentActionRecordItem;
import net.akive.api.legal.application.provided.dto.LegalDocumentActionsRecordCommand;
import net.akive.api.legal.domain.LegalDocumentActionType;
import net.akive.api.user.application.required.data.SignUpLegalDocumentActionData;
import net.akive.api.user.domain.SignUpLegalDocumentActionType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static net.akive.api.legal.domain.LegalDocumentActionContext.SIGN_UP;
import static net.akive.api.legal.domain.fixture.LegalDocumentActionFixture.REQUEST_ID;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.LEGAL_DOCUMENT_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SignUpLegalDocumentActionRecorderAdapterTest {

    @Mock
    LegalDocumentActionRecorder legalDocumentActionRecorder;

    @InjectMocks
    SignUpLegalDocumentActionRecorderAdapter signUpLegalDocumentActionRecorderAdapter;

    @ParameterizedTest
    @MethodSource("actionTypeMappings")
    void 회원_가입_시점의_약관_및_정책_문서에_대한_응답을_기록한다(
            SignUpLegalDocumentActionType actionType,
            LegalDocumentActionType expectedActionType
    ) {
        SignUpLegalDocumentActionData legalDocumentAction = new SignUpLegalDocumentActionData(
                REQUEST_ID,
                LEGAL_DOCUMENT_ID,
                actionType
        );

        signUpLegalDocumentActionRecorderAdapter.record(USER_ID, List.of(legalDocumentAction));

        ArgumentCaptor<LegalDocumentActionsRecordCommand> commandCaptor =
                ArgumentCaptor.forClass(LegalDocumentActionsRecordCommand.class);
        then(legalDocumentActionRecorder).should().record(commandCaptor.capture());

        LegalDocumentActionsRecordCommand command = commandCaptor.getValue();
        assertThat(command.userId()).isEqualTo(USER_ID);
        assertThat(command.actionContext()).isEqualTo(SIGN_UP);
        assertThat(command.actionRecordItems()).containsExactly(
                new LegalDocumentActionRecordItem(
                        REQUEST_ID,
                        LEGAL_DOCUMENT_ID,
                        expectedActionType
                )
        );
    }

    private static Stream<Arguments> actionTypeMappings() {
        return Stream.of(
                Arguments.of(SignUpLegalDocumentActionType.AGREED, LegalDocumentActionType.AGREED),
                Arguments.of(SignUpLegalDocumentActionType.DECLINED, LegalDocumentActionType.DECLINED),
                Arguments.of(SignUpLegalDocumentActionType.ACKNOWLEDGED, LegalDocumentActionType.ACKNOWLEDGED),
                Arguments.of(SignUpLegalDocumentActionType.CONFIRMED, LegalDocumentActionType.CONFIRMED)
        );
    }
}
