package net.akive.api.legal.application.service;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.legal.application.provided.LegalDocumentFinder;
import net.akive.api.legal.application.provided.dto.LegalRequirementCheckResult;
import net.akive.api.legal.application.required.LegalDocumentActionRepository;
import net.akive.api.legal.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static net.akive.api.common.exception.business.ResourceNotFoundError.CURRENT_LEGAL_DOCUMENT_NOT_FOUND;
import static net.akive.api.legal.domain.LegalDocumentActionContext.CONSENT_MANAGEMENT;
import static net.akive.api.legal.domain.LegalDocumentActionType.*;
import static net.akive.api.legal.domain.LegalDocumentType.*;
import static net.akive.api.legal.domain.LegalInteractionType.*;
import static net.akive.api.legal.domain.LegalRequirementScope.AI_FEATURE;
import static net.akive.api.legal.domain.LegalRequirementScope.CORE_SERVICE;
import static net.akive.api.legal.domain.fixture.LegalDocumentActionFixture.*;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.reconstructLegalDocument;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LegalRequirementQueryServiceTest {

    private static final Map<LegalDocumentType, Long> LEGAL_DOCUMENT_ID_BY_TYPE = Map.of(
            SERVICE_TERMS, 1L,
            PRIVACY_POLICY, 2L,
            AGE_14_CONFIRMATION, 3L,
            AI_LIMITATIONS, 4L,
            AI_PERSONAL_DATA, 5L,
            OPENAI_OVERSEAS_TRANSFER, 6L
    );

    @Mock
    LegalDocumentFinder legalDocumentFinder;

    @Mock
    LegalDocumentActionRepository legalDocumentActionRepository;

    @InjectMocks
    LegalRequirementQueryService legalRequirementQueryService;

    @Test
    void 서비스_이용약관에_동의하고_개인정보_처리방침과_만_14세_이상을_확인하면_기본_서비스_이용_요건을_충족한다() {
        givenCurrentDocumentIds(CORE_SERVICE);
        givenLatestAction(SERVICE_TERMS, AGREEMENT, AGREED);
        givenLatestAction(PRIVACY_POLICY, ACKNOWLEDGEMENT, ACKNOWLEDGED);
        givenLatestAction(AGE_14_CONFIRMATION, CONFIRMATION, CONFIRMED);

        LegalRequirementCheckResult result = legalRequirementQueryService.check(USER_ID, CORE_SERVICE);

        assertThat(result.satisfied()).isTrue();
        assertThat(result.unsatisfiedDocumentTypes()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("coreServiceUnansweredDocumentTypes")
    void 서비스_이용약관에_동의하지_않거나_개인정보_처리방침_또는_만_14세_이상임을_확인하지_않으면_기본_서비스_이용_요건을_충족하지_않는다(
            LegalDocumentType unansweredDocumentType
    ) {
        givenCurrentDocumentIds(CORE_SERVICE);
        givenRequiredActionsExcept(CORE_SERVICE, unansweredDocumentType);

        LegalRequirementCheckResult result = legalRequirementQueryService.check(USER_ID, CORE_SERVICE);

        assertThat(result.satisfied()).isFalse();
        assertThat(result.unsatisfiedDocumentTypes()).containsExactly(unansweredDocumentType);
    }

    @Test
    void AI_이용_유의사항을_확인하고_AI_개인정보_처리와_OpenAI_국외_이전에_동의하면_AI_기능_이용_요건을_충족한다() {
        givenCurrentDocumentIds(AI_FEATURE);
        givenLatestAction(AI_LIMITATIONS, ACKNOWLEDGEMENT, ACKNOWLEDGED);
        givenLatestAction(AI_PERSONAL_DATA, CONSENT, AGREED);
        givenLatestAction(OPENAI_OVERSEAS_TRANSFER, CONSENT, AGREED);

        LegalRequirementCheckResult result = legalRequirementQueryService.check(USER_ID, AI_FEATURE);

        assertThat(result.satisfied()).isTrue();
        assertThat(result.unsatisfiedDocumentTypes()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("aiConsentDocumentTypes")
    void AI_개인정보_처리_또는_OpenAI_국외_이전_동의를_철회하면_AI_기능_이용_요건을_충족하지_않는다(
            LegalDocumentType revokedDocumentType
    ) {
        givenCurrentDocumentIds(AI_FEATURE);
        givenRequiredActionsWith(AI_FEATURE, revokedDocumentType, REVOKED);

        LegalRequirementCheckResult result = legalRequirementQueryService.check(USER_ID, AI_FEATURE);

        assertThat(result.satisfied()).isFalse();
        assertThat(result.unsatisfiedDocumentTypes()).containsExactly(revokedDocumentType);
    }

    @ParameterizedTest
    @MethodSource("aiFeatureUnansweredDocumentTypes")
    void AI_이용_유의사항을_확인하지_않거나_AI_개인정보_처리_또는_OpenAI_국외_이전에_동의하지_않으면_AI_기능_이용_요건을_충족하지_않는다(
            LegalDocumentType unansweredDocumentType
    ) {
        givenCurrentDocumentIds(AI_FEATURE);
        givenRequiredActionsExcept(AI_FEATURE, unansweredDocumentType);

        LegalRequirementCheckResult result = legalRequirementQueryService.check(USER_ID, AI_FEATURE);

        assertThat(result.satisfied()).isFalse();
        assertThat(result.unsatisfiedDocumentTypes()).containsExactly(unansweredDocumentType);
    }

    @ParameterizedTest
    @MethodSource("requirementScopeAndUnconfiguredDocumentType")
    void 이용_요건에_필요한_현행_약관_및_정책_문서를_조회할_수_없으면_예외를_던진다(
            LegalRequirementScope requirementScope,
            LegalDocumentType unconfiguredDocumentType
    ) {
        givenOneCurrentDocumentIsNotConfigured(unconfiguredDocumentType);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> legalRequirementQueryService.check(USER_ID, requirementScope))
                .withMessage(
                        "Current legal document required for service access is not configured: documentType=%s",
                        unconfiguredDocumentType
                );
    }

    private void givenCurrentDocumentIds(LegalRequirementScope scope) {
        LegalPolicy.requirementsFor(scope).keySet().forEach(documentType ->
                given(legalDocumentFinder.findCurrentByDocumentType(documentType))
                        .willReturn(reconstructLegalDocument(
                                LEGAL_DOCUMENT_ID_BY_TYPE.get(documentType),
                                documentType,
                                interactionTypeFor(documentType)
                        ))
        );
    }

    private void givenLatestAction(
            LegalDocumentType documentType,
            LegalInteractionType interactionType,
            LegalDocumentActionType actionType
    ) {
        Long legalDocumentId = LEGAL_DOCUMENT_ID_BY_TYPE.get(documentType);
        LegalDocument legalDocument = reconstructLegalDocument(
                legalDocumentId,
                documentType,
                interactionType
        );
        LegalDocumentAction legalDocumentAction = LegalDocumentAction.reconstruct(
                legalDocumentId,
                createReconstructedUser(),
                legalDocument,
                actionType,
                CONSENT_MANAGEMENT,
                REQUEST_ID,
                ACTED_AT,
                CREATED_AT
        );

        given(legalDocumentActionRepository.findLatestByUserIdAndLegalDocumentId(
                USER_ID,
                legalDocumentId
        )).willReturn(Optional.of(legalDocumentAction));
    }

    private void givenRequiredActionsExcept(
            LegalRequirementScope requirementScope,
            LegalDocumentType unansweredDocumentType
    ) {
        LegalPolicy.requirementsFor(requirementScope).forEach((documentType, requiredActionType) -> {
            if (documentType == unansweredDocumentType) {
                givenNoLatestAction(documentType);
                return;
            }
            givenLatestAction(documentType, interactionTypeFor(documentType), requiredActionType);
        });
    }

    private void givenNoLatestAction(LegalDocumentType documentType) {
        given(legalDocumentActionRepository.findLatestByUserIdAndLegalDocumentId(
                USER_ID,
                LEGAL_DOCUMENT_ID_BY_TYPE.get(documentType)
        )).willReturn(Optional.empty());
    }

    private void givenRequiredActionsWith(
            LegalRequirementScope requirementScope,
            LegalDocumentType targetDocumentType,
            LegalDocumentActionType targetActionType
    ) {
        LegalPolicy.requirementsFor(requirementScope).forEach((documentType, requiredActionType) ->
                givenLatestAction(
                        documentType,
                        interactionTypeFor(documentType),
                        documentType == targetDocumentType ? targetActionType : requiredActionType
                )
        );
    }

    private void givenOneCurrentDocumentIsNotConfigured(LegalDocumentType unconfiguredDocumentType) {
        given(legalDocumentFinder.findCurrentByDocumentType(any(LegalDocumentType.class)))
                .willAnswer(invocation -> {
                    LegalDocumentType documentType = invocation.getArgument(0);
                    if (documentType == unconfiguredDocumentType) {
                        throw new ResourceNotFoundException(CURRENT_LEGAL_DOCUMENT_NOT_FOUND);
                    }
                    return reconstructLegalDocument(
                            LEGAL_DOCUMENT_ID_BY_TYPE.get(documentType),
                            documentType,
                            interactionTypeFor(documentType)
                    );
                });
    }

    private static Stream<LegalDocumentType> coreServiceUnansweredDocumentTypes() {
        return Stream.of(
                SERVICE_TERMS,
                PRIVACY_POLICY,
                AGE_14_CONFIRMATION
        );
    }

    private static Stream<LegalDocumentType> aiConsentDocumentTypes() {
        return Stream.of(
                AI_PERSONAL_DATA,
                OPENAI_OVERSEAS_TRANSFER
        );
    }

    private static Stream<LegalDocumentType> aiFeatureUnansweredDocumentTypes() {
        return Stream.of(
                AI_LIMITATIONS,
                AI_PERSONAL_DATA,
                OPENAI_OVERSEAS_TRANSFER
        );
    }

    private static Stream<Arguments> requirementScopeAndUnconfiguredDocumentType() {
        return Stream.of(
                Arguments.of(CORE_SERVICE, SERVICE_TERMS),
                Arguments.of(CORE_SERVICE, AGE_14_CONFIRMATION),
                Arguments.of(AI_FEATURE, AI_LIMITATIONS),
                Arguments.of(AI_FEATURE, AI_PERSONAL_DATA)
        );
    }

    private LegalInteractionType interactionTypeFor(LegalDocumentType documentType) {
        return switch (documentType) {
            case SERVICE_TERMS -> AGREEMENT;
            case PRIVACY_POLICY, AI_LIMITATIONS -> ACKNOWLEDGEMENT;
            case AGE_14_CONFIRMATION -> CONFIRMATION;
            case POSTHOG_ANALYTICS, POSTHOG_OVERSEAS_TRANSFER,
                 AI_PERSONAL_DATA, OPENAI_OVERSEAS_TRANSFER -> CONSENT;
        };
    }
}
