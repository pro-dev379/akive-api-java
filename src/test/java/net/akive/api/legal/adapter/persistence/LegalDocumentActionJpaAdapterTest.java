package net.akive.api.legal.adapter.persistence;

import net.akive.api.legal.application.required.LegalDocumentActionRepository;
import net.akive.api.legal.application.required.LegalDocumentRepository;
import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentAction;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static net.akive.api.legal.domain.LegalDocumentType.PRIVACY_POLICY;
import static net.akive.api.legal.domain.fixture.LegalDocumentActionFixture.*;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.createLegalDocument;
import static net.akive.api.user.domain.fixture.UserFixture.ANOTHER_EMAIL;
import static net.akive.api.user.domain.fixture.UserFixture.ANOTHER_NICKNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LegalDocumentActionJpaAdapterTest extends RepositoryTestSupport {

    private static final UUID LATEST_REQUEST_ID =
            UUID.fromString("364c1293-d821-42aa-950b-e574949bfb49");
    private static final UUID ANOTHER_USER_REQUEST_ID =
            UUID.fromString("e2246495-a42a-4903-83b8-637ae68ab5a6");
    private static final UUID ANOTHER_LEGAL_DOCUMENT_REQUEST_ID =
            UUID.fromString("aab6faf8-10bd-4e55-982d-2ae9a71c3327");

    @Autowired
    LegalDocumentActionRepository legalDocumentActionRepository;

    @Autowired
    LegalDocumentRepository legalDocumentRepository;

    @Test
    void 약관_및_정책_문서에_대한_회원_응답을_저장한다() {
        User savedUser = saveUser();
        LegalDocument savedLegalDocument = legalDocumentRepository.save(createLegalDocument());
        LegalDocumentAction legalDocumentAction = createLegalDocumentAction(
                savedUser,
                savedLegalDocument,
                REQUEST_ID,
                ACTED_AT
        );

        LegalDocumentAction savedLegalDocumentAction = legalDocumentActionRepository.save(legalDocumentAction);
        flushAndClear();

        LegalDocumentAction foundLegalDocumentAction = legalDocumentActionRepository
                .findLatestByUserIdAndLegalDocumentId(
                        savedUser.getUserId(),
                        savedLegalDocument.getLegalDocumentId()
                )
                .orElseThrow();

        assertThat(foundLegalDocumentAction).isEqualTo(savedLegalDocumentAction);
        assertThat(foundLegalDocumentAction.getUser()).isEqualTo(legalDocumentAction.getUser());
        assertThat(foundLegalDocumentAction.getLegalDocument()).isEqualTo(legalDocumentAction.getLegalDocument());
        assertThat(foundLegalDocumentAction.getActionType()).isEqualTo(legalDocumentAction.getActionType());
        assertThat(foundLegalDocumentAction.getContext()).isEqualTo(legalDocumentAction.getContext());
        assertThat(foundLegalDocumentAction.getRequestId()).isEqualTo(legalDocumentAction.getRequestId());
        assertThat(foundLegalDocumentAction.getActedAt()).isEqualTo(legalDocumentAction.getActedAt());
        assertThat(foundLegalDocumentAction.getCreatedAt()).isNotNull();
    }

    @Test
    void 같은_요청_식별자로_약관_및_정책_문서를_중복_저장할_수_없다() {
        User savedUser = saveUser();
        LegalDocument savedLegalDocument = legalDocumentRepository.save(createLegalDocument());
        legalDocumentActionRepository.save(createLegalDocumentAction(
                savedUser,
                savedLegalDocument,
                REQUEST_ID,
                ACTED_AT
        ));

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> {
                    legalDocumentActionRepository.save(createLegalDocumentAction(
                            savedUser,
                            savedLegalDocument,
                            REQUEST_ID,
                            ACTED_AT.plusSeconds(1)
                    ));
                    flushAndClear();
                });
    }

    @Test
    void 회원의_특정_약관_및_정책_문서에_대한_가장_최근_응답을_조회한다() {
        User savedUser = saveUser();
        User anotherSavedUser = saveUser(ANOTHER_EMAIL, ANOTHER_NICKNAME);
        LegalDocument savedLegalDocument = legalDocumentRepository.save(createLegalDocument());
        LegalDocument anotherSavedLegalDocument = legalDocumentRepository.save(createLegalDocument(PRIVACY_POLICY));
        Instant latestActedAt = ACTED_AT.plusSeconds(1);
        LegalDocumentAction latestLegalDocumentAction = legalDocumentActionRepository.save(
                createLegalDocumentAction(
                        savedUser,
                        savedLegalDocument,
                        LATEST_REQUEST_ID,
                        latestActedAt
                )
        );
        legalDocumentActionRepository.save(createLegalDocumentAction(
                savedUser,
                savedLegalDocument,
                REQUEST_ID,
                ACTED_AT
        ));
        legalDocumentActionRepository.save(createLegalDocumentAction(
                anotherSavedUser,
                savedLegalDocument,
                ANOTHER_USER_REQUEST_ID,
                latestActedAt.plusSeconds(1)
        ));
        legalDocumentActionRepository.save(createLegalDocumentAction(
                savedUser,
                anotherSavedLegalDocument,
                ANOTHER_LEGAL_DOCUMENT_REQUEST_ID,
                latestActedAt.plusSeconds(1)
        ));
        flushAndClear();

        Optional<LegalDocumentAction> foundLegalDocumentAction =
                legalDocumentActionRepository.findLatestByUserIdAndLegalDocumentId(
                        savedUser.getUserId(),
                        savedLegalDocument.getLegalDocumentId()
                );

        assertThat(foundLegalDocumentAction).contains(latestLegalDocumentAction);
    }
}
