package net.akive.api.legal.application.service;

import net.akive.api.legal.application.provided.LegalDocumentFinder;
import net.akive.api.legal.application.provided.dto.LegalDocumentActionStatusResult;
import net.akive.api.legal.application.required.LegalDocumentActionRepository;
import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static net.akive.api.legal.domain.LegalDocumentActionContext.CONSENT_MANAGEMENT;
import static net.akive.api.legal.domain.LegalDocumentActionType.AGREED;
import static net.akive.api.legal.domain.LegalDocumentType.POSTHOG_ANALYTICS;
import static net.akive.api.legal.domain.LegalDocumentType.POSTHOG_OVERSEAS_TRANSFER;
import static net.akive.api.legal.domain.LegalInteractionType.CONSENT;
import static net.akive.api.legal.domain.fixture.LegalDocumentActionFixture.ANOTHER_LEGAL_DOCUMENT_ACTION_ID;
import static net.akive.api.legal.domain.fixture.LegalDocumentActionFixture.ANOTHER_REQUEST_ID;
import static net.akive.api.legal.domain.fixture.LegalDocumentActionFixture.LEGAL_DOCUMENT_ACTION_ID;
import static net.akive.api.legal.domain.fixture.LegalDocumentActionFixture.REQUEST_ID;
import static net.akive.api.legal.domain.fixture.LegalDocumentActionFixture.reconstructLegalDocumentAction;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.ANOTHER_LEGAL_DOCUMENT_ID;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.LEGAL_DOCUMENT_ID;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.reconstructLegalDocument;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LegalDocumentActionQueryServiceTest {

    @Mock
    LegalDocumentActionRepository legalDocumentActionRepository;

    @Mock
    LegalDocumentFinder legalDocumentFinder;

    @InjectMocks
    LegalDocumentActionQueryService legalDocumentActionQueryService;

    @Test
    void 응답_컨텍스트별_현행_약관_및_정책_문서와_회원_응답_상태를_조회한다() {
        List<LegalDocument> currentDocuments = reconstructCurrentDocuments();
        List<LegalDocumentAction> latestActions = reconstructLatestActions(currentDocuments);
        givenCurrentDocumentsAndLatestActions(currentDocuments, latestActions);

        List<LegalDocumentActionStatusResult> actionStatusResults =
                legalDocumentActionQueryService.findAllCurrentByContext(USER_ID, CONSENT_MANAGEMENT);

        assertThat(actionStatusResults).containsExactly(
                new LegalDocumentActionStatusResult(
                        currentDocuments.getFirst(),
                        Optional.of(latestActions.getFirst())
                ),
                new LegalDocumentActionStatusResult(
                        currentDocuments.getLast(),
                        Optional.of(latestActions.getLast())
                )
        );
        then(legalDocumentFinder).should().findAllCurrentByContext(CONSENT_MANAGEMENT);
        then(legalDocumentActionRepository).should()
                .findAllLatestByUserIdAndLegalDocumentIdIn(USER_ID, findLegalDocumentIds(currentDocuments));
    }

    @Test
    void 현행_약관_및_정책_문서에_대한_회원_응답이_없으면_빈_응답_상태를_반환한다() {
        List<LegalDocument> currentDocuments = reconstructCurrentDocuments();
        givenCurrentDocumentsAndLatestActions(currentDocuments, List.of());

        List<LegalDocumentActionStatusResult> actionStatusResults =
                legalDocumentActionQueryService.findAllCurrentByContext(USER_ID, CONSENT_MANAGEMENT);

        assertThat(actionStatusResults).containsExactly(
                new LegalDocumentActionStatusResult(currentDocuments.getFirst(), Optional.empty()),
                new LegalDocumentActionStatusResult(currentDocuments.getLast(), Optional.empty())
        );
        then(legalDocumentFinder).should().findAllCurrentByContext(CONSENT_MANAGEMENT);
        then(legalDocumentActionRepository).should()
                .findAllLatestByUserIdAndLegalDocumentIdIn(USER_ID, findLegalDocumentIds(currentDocuments));
    }

    private List<LegalDocument> reconstructCurrentDocuments() {
        return List.of(
                reconstructLegalDocument(LEGAL_DOCUMENT_ID, POSTHOG_ANALYTICS, CONSENT),
                reconstructLegalDocument(ANOTHER_LEGAL_DOCUMENT_ID, POSTHOG_OVERSEAS_TRANSFER, CONSENT)
        );
    }

    private List<LegalDocumentAction> reconstructLatestActions(List<LegalDocument> currentDocuments) {
        return List.of(
                reconstructLegalDocumentAction(
                        LEGAL_DOCUMENT_ACTION_ID,
                        currentDocuments.getFirst(),
                        AGREED,
                        REQUEST_ID
                ),
                reconstructLegalDocumentAction(
                        ANOTHER_LEGAL_DOCUMENT_ACTION_ID,
                        currentDocuments.getLast(),
                        AGREED,
                        ANOTHER_REQUEST_ID
                )
        );
    }

    private void givenCurrentDocumentsAndLatestActions(
            List<LegalDocument> currentDocuments,
            List<LegalDocumentAction> latestActions
    ) {
        given(legalDocumentFinder.findAllCurrentByContext(CONSENT_MANAGEMENT))
                .willReturn(currentDocuments);
        given(legalDocumentActionRepository.findAllLatestByUserIdAndLegalDocumentIdIn(
                USER_ID,
                findLegalDocumentIds(currentDocuments)
        )).willReturn(latestActions);
    }

    private List<Long> findLegalDocumentIds(List<LegalDocument> legalDocuments) {
        return legalDocuments.stream().map(LegalDocument::getLegalDocumentId).toList();
    }
}
