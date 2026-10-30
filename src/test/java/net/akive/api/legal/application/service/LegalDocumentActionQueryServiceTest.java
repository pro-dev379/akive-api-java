package net.akive.api.legal.application.service;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.legal.application.required.LegalDocumentActionRepository;
import net.akive.api.legal.domain.LegalDocumentAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.LEGAL_DOCUMENT_ACTION_NOT_FOUND;
import static net.akive.api.legal.domain.LegalDocumentActionType.AGREED;
import static net.akive.api.legal.domain.LegalInteractionType.CONSENT;
import static net.akive.api.legal.domain.fixture.LegalDocumentActionFixture.LEGAL_DOCUMENT_ACTION_ID;
import static net.akive.api.legal.domain.fixture.LegalDocumentActionFixture.reconstructLegalDocumentAction;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.LEGAL_DOCUMENT_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LegalDocumentActionQueryServiceTest {

    @Mock
    LegalDocumentActionRepository legalDocumentActionRepository;

    @InjectMocks
    LegalDocumentActionQueryService legalDocumentActionQueryService;

    @Test
    void 회원의_현행_약관_및_정책_문서_응답을_조회한다() {
        LegalDocumentAction legalDocumentAction = reconstructLegalDocumentAction(
                LEGAL_DOCUMENT_ACTION_ID,
                CONSENT,
                AGREED
        );
        given(legalDocumentActionRepository.findLatestByUserIdAndLegalDocumentId(USER_ID, LEGAL_DOCUMENT_ID))
                .willReturn(Optional.of(legalDocumentAction));

        LegalDocumentAction foundLegalDocumentAction = legalDocumentActionQueryService.findCurrentOrThrow(
                USER_ID,
                LEGAL_DOCUMENT_ID
        );

        assertThat(foundLegalDocumentAction).isEqualTo(legalDocumentAction);
        then(legalDocumentActionRepository).should()
                .findLatestByUserIdAndLegalDocumentId(USER_ID, LEGAL_DOCUMENT_ID);
    }

    @Test
    void 회원의_현행_약관_및_정책_문서_응답이_없으면_예외를_던진다() {
        given(legalDocumentActionRepository.findLatestByUserIdAndLegalDocumentId(USER_ID, LEGAL_DOCUMENT_ID))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> legalDocumentActionQueryService.findCurrentOrThrow(
                        USER_ID,
                        LEGAL_DOCUMENT_ID
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(LEGAL_DOCUMENT_ACTION_NOT_FOUND);

        then(legalDocumentActionRepository).should()
                .findLatestByUserIdAndLegalDocumentId(USER_ID, LEGAL_DOCUMENT_ID);
    }
}
