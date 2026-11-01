package net.akive.api.legal.domain.fixture;

import net.akive.api.legal.domain.LegalDocumentAction;
import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentActionType;
import net.akive.api.legal.domain.LegalInteractionType;
import net.akive.api.user.domain.User;

import java.time.Instant;
import java.util.UUID;

import static net.akive.api.legal.domain.LegalDocumentActionContext.SIGN_UP;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.reconstructLegalDocument;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;

public final class LegalDocumentActionFixture {

    public static final Long LEGAL_DOCUMENT_ACTION_ID = 1L;
    public static final Long ANOTHER_LEGAL_DOCUMENT_ACTION_ID = 2L;
    public static final UUID REQUEST_ID = UUID.fromString("4c0d152f-d54e-4a9e-9745-bc96f4d94fe2");
    public static final UUID ANOTHER_REQUEST_ID = UUID.fromString("e2246495-a42a-4903-83b8-637ae68ab5a6");
    public static final Instant ACTED_AT = Instant.parse("2026-10-29T03:00:00Z");
    public static final Instant CREATED_AT = Instant.parse("2026-10-29T03:00:01Z");

    private LegalDocumentActionFixture() {
    }

    public static LegalDocumentAction createLegalDocumentAction(
            User user,
            LegalDocument legalDocument,
            UUID requestId,
            Instant actedAt
    ) {
        return LegalDocumentAction.create(
                user,
                legalDocument,
                LegalDocumentActionType.AGREED,
                SIGN_UP,
                requestId,
                actedAt
        );
    }

    public static LegalDocumentAction reconstructLegalDocumentAction(
            Long legalDocumentActionId,
            LegalInteractionType interactionType,
            LegalDocumentActionType actionType
    ) {
        return reconstructLegalDocumentAction(
                legalDocumentActionId,
                reconstructLegalDocument(interactionType),
                actionType,
                REQUEST_ID
        );
    }

    public static LegalDocumentAction reconstructLegalDocumentAction(
            Long legalDocumentActionId,
            LegalDocument legalDocument,
            LegalDocumentActionType actionType,
            UUID requestId
    ) {
        return LegalDocumentAction.reconstruct(
                legalDocumentActionId,
                createReconstructedUser(),
                legalDocument,
                actionType,
                SIGN_UP,
                requestId,
                ACTED_AT,
                CREATED_AT
        );
    }
}
