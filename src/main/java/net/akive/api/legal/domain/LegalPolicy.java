package net.akive.api.legal.domain;

import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.legal.domain.LegalDocumentActionType.*;
import static net.akive.api.legal.domain.LegalDocumentType.*;

@NoArgsConstructor(access = PRIVATE)
public final class LegalPolicy {

    public static final int DOCUMENT_VERSION_MAX_LENGTH = 30;

    private static final List<LegalDocumentType> SIGN_UP_DOCUMENT_TYPES = List.of(
            SERVICE_TERMS,
            PRIVACY_POLICY,
            AGE_14_CONFIRMATION,
            POSTHOG_ANALYTICS,
            POSTHOG_OVERSEAS_TRANSFER
    );

    private static final List<LegalDocumentType> AI_FIRST_USE_DOCUMENT_TYPES = List.of(
            AI_LIMITATIONS,
            AI_PERSONAL_DATA,
            OPENAI_OVERSEAS_TRANSFER
    );

    private static final List<LegalDocumentType> CONSENT_MANAGEMENT_DOCUMENT_TYPES = List.of(
            POSTHOG_ANALYTICS,
            POSTHOG_OVERSEAS_TRANSFER,
            AI_PERSONAL_DATA,
            OPENAI_OVERSEAS_TRANSFER
    );

    private static final Map<LegalDocumentType, LegalDocumentActionType> CORE_SERVICE_REQUIREMENTS = Map.of(
            SERVICE_TERMS, AGREED,
            PRIVACY_POLICY, ACKNOWLEDGED,
            AGE_14_CONFIRMATION, CONFIRMED
    );

    private static final Map<LegalDocumentType, LegalDocumentActionType> AI_FEATURE_REQUIREMENTS = Map.of(
            AI_LIMITATIONS, ACKNOWLEDGED,
            AI_PERSONAL_DATA, AGREED,
            OPENAI_OVERSEAS_TRANSFER, AGREED
    );

    public static List<LegalDocumentType> documentTypesFor(LegalDocumentActionContext context) {
        return switch (requireNonNull(context)) {
            case SIGN_UP, EXISTING_USER_ONBOARDING -> SIGN_UP_DOCUMENT_TYPES;
            case AI_FIRST_USE -> AI_FIRST_USE_DOCUMENT_TYPES;
            case CONSENT_MANAGEMENT -> CONSENT_MANAGEMENT_DOCUMENT_TYPES;
            case POLICY_UPDATE -> throw new IllegalArgumentException(
                    "Unsupported legal document query context: " + context
            );
        };
    }

    public static Map<LegalDocumentType, LegalDocumentActionType> requirementsFor(LegalRequirementScope scope) {
        return switch (requireNonNull(scope)) {
            case CORE_SERVICE -> CORE_SERVICE_REQUIREMENTS;
            case AI_FEATURE -> AI_FEATURE_REQUIREMENTS;
        };
    }
}
