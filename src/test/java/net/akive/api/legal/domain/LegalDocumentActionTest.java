package net.akive.api.legal.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_LEGAL_DOCUMENT_ACTION_TYPE_FOR_INTERACTION_TYPE;
import static net.akive.api.legal.domain.LegalDocumentActionContext.SIGN_UP;
import static net.akive.api.legal.domain.LegalDocumentActionType.*;
import static net.akive.api.legal.domain.LegalInteractionType.*;
import static net.akive.api.legal.domain.fixture.LegalDocumentActionFixture.*;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.reconstructLegalDocument;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.*;

class LegalDocumentActionTest {

    @Test
    void 저장된_약관_및_정책_문서에_대한_회원_응답을_복원한다() {
        User user = createReconstructedUser();
        LegalDocument legalDocument = reconstructLegalDocument(CONSENT);

        LegalDocumentAction legalDocumentAction = LegalDocumentAction.reconstruct(
                LEGAL_DOCUMENT_ACTION_ID,
                user,
                legalDocument,
                AGREED,
                SIGN_UP,
                REQUEST_ID,
                ACTED_AT,
                CREATED_AT
        );

        assertThat(legalDocumentAction.getLegalDocumentActionId()).isEqualTo(LEGAL_DOCUMENT_ACTION_ID);
        assertThat(legalDocumentAction.getUser()).isEqualTo(user);
        assertThat(legalDocumentAction.getLegalDocument()).isEqualTo(legalDocument);
        assertThat(legalDocumentAction.getActionType()).isEqualTo(AGREED);
        assertThat(legalDocumentAction.getContext()).isEqualTo(SIGN_UP);
        assertThat(legalDocumentAction.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(legalDocumentAction.getActedAt()).isEqualTo(ACTED_AT);
        assertThat(legalDocumentAction.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void 약관_및_정책_문서에_대한_회원_응답을_생성한다() {
        User user = createReconstructedUser();
        LegalDocument legalDocument = reconstructLegalDocument(CONSENT);

        LegalDocumentAction legalDocumentAction = LegalDocumentAction.create(
                user,
                legalDocument,
                AGREED,
                SIGN_UP,
                REQUEST_ID,
                ACTED_AT
        );

        assertThat(legalDocumentAction.getLegalDocumentActionId()).isNull();
        assertThat(legalDocumentAction.getUser()).isEqualTo(user);
        assertThat(legalDocumentAction.getLegalDocument()).isEqualTo(legalDocument);
        assertThat(legalDocumentAction.getActionType()).isEqualTo(AGREED);
        assertThat(legalDocumentAction.getContext()).isEqualTo(SIGN_UP);
        assertThat(legalDocumentAction.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(legalDocumentAction.getActedAt()).isEqualTo(ACTED_AT);
        assertThat(legalDocumentAction.getCreatedAt()).isNull();
    }

    @ParameterizedTest
    @MethodSource("validInteractionTypeAndActionPairs")
    void 약관_및_정책_문서가_요구하는_응답에_맞는_회원_응답을_생성한다(
            LegalInteractionType interactionType,
            LegalDocumentActionType actionType
    ) {
        assertThatCode(() -> LegalDocumentAction.create(
                createReconstructedUser(),
                reconstructLegalDocument(interactionType),
                actionType,
                SIGN_UP,
                REQUEST_ID,
                ACTED_AT
        )).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("invalidInteractionTypeAndActionPairs")
    void 약관_및_정책_문서가_요구하는_응답과_맞지_않는_회원_응답은_생성할_수_없다(
            LegalInteractionType interactionType,
            LegalDocumentActionType actionType
    ) {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> LegalDocumentAction.create(
                        createReconstructedUser(),
                        reconstructLegalDocument(interactionType),
                        actionType,
                        SIGN_UP,
                        REQUEST_ID,
                        ACTED_AT
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_LEGAL_DOCUMENT_ACTION_TYPE_FOR_INTERACTION_TYPE);
    }

    @Test
    void 같은_식별자를_가진_약관_및_정책_문서에_대한_회원_응답은_같게_취급한다() {
        LegalDocumentAction legalDocumentAction = reconstructLegalDocumentAction(
                LEGAL_DOCUMENT_ACTION_ID,
                CONSENT,
                AGREED
        );
        LegalDocumentAction sameLegalDocumentAction = reconstructLegalDocumentAction(
                LEGAL_DOCUMENT_ACTION_ID,
                CONSENT,
                DECLINED
        );

        assertThat(legalDocumentAction).isEqualTo(sameLegalDocumentAction);
        assertThat(legalDocumentAction.hashCode()).isEqualTo(sameLegalDocumentAction.hashCode());
    }

    @Test
    void 식별자가_다른_약관_및_정책_문서에_대한_회원_응답은_다르게_취급한다() {
        LegalDocumentAction legalDocumentAction = reconstructLegalDocumentAction(
                LEGAL_DOCUMENT_ACTION_ID,
                CONSENT,
                AGREED
        );
        LegalDocumentAction anotherLegalDocumentAction = reconstructLegalDocumentAction(
                ANOTHER_LEGAL_DOCUMENT_ACTION_ID,
                CONSENT,
                AGREED
        );

        assertThat(legalDocumentAction).isNotEqualTo(anotherLegalDocumentAction);
    }

    @Test
    void 식별자가_없는_약관_및_정책_문서에_대한_회원_응답끼리는_나머지_값이_같더라도_다르게_취급한다() {
        User user = createReconstructedUser();
        LegalDocument legalDocument = reconstructLegalDocument(CONSENT);
        LegalDocumentAction legalDocumentAction = LegalDocumentAction.create(
                user,
                legalDocument,
                AGREED,
                SIGN_UP,
                REQUEST_ID,
                ACTED_AT
        );
        LegalDocumentAction anotherLegalDocumentAction = LegalDocumentAction.create(
                user,
                legalDocument,
                AGREED,
                SIGN_UP,
                REQUEST_ID,
                ACTED_AT
        );

        assertThat(legalDocumentAction).isNotEqualTo(anotherLegalDocumentAction);
    }

    private static Stream<Arguments> validInteractionTypeAndActionPairs() {
        return Stream.of(
                Arguments.of(AGREEMENT, AGREED),
                Arguments.of(ACKNOWLEDGEMENT, ACKNOWLEDGED),
                Arguments.of(CONFIRMATION, CONFIRMED),
                Arguments.of(CONSENT, AGREED),
                Arguments.of(CONSENT, DECLINED),
                Arguments.of(CONSENT, REVOKED)
        );
    }

    private static Stream<Arguments> invalidInteractionTypeAndActionPairs() {
        return Stream.of(
                invalidPairs(AGREEMENT, Set.of(AGREED)),
                invalidPairs(ACKNOWLEDGEMENT, Set.of(ACKNOWLEDGED)),
                invalidPairs(CONFIRMATION, Set.of(CONFIRMED)),
                invalidPairs(CONSENT, Set.of(AGREED, DECLINED, REVOKED))
        ).flatMap(stream -> stream);
    }

    private static Stream<Arguments> invalidPairs(
            LegalInteractionType interactionType,
            Set<LegalDocumentActionType> validActionTypes
    ) {
        return Arrays.stream(LegalDocumentActionType.values())
                .filter(actionType -> !validActionTypes.contains(actionType))
                .map(actionType -> Arguments.of(interactionType, actionType));
    }
}
