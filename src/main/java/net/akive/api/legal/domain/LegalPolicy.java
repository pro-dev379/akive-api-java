package net.akive.api.legal.domain;

import lombok.NoArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;
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

    public static List<LegalDocumentType> documentTypesFor(LegalDocumentActionContext context) {
        return switch (requireNonNull(context)) {
            case SIGN_UP, EXISTING_USER_ONBOARDING -> SIGN_UP_DOCUMENT_TYPES;
            case AI_FIRST_USE -> AI_FIRST_USE_DOCUMENT_TYPES;
            case SETTINGS, POLICY_UPDATE -> throw new IllegalArgumentException(
                    "Unsupported legal document query context: " + context
            );
        };
    }
}
