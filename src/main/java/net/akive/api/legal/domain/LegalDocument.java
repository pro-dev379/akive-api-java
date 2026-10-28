package net.akive.api.legal.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_LEGAL_DOCUMENT_DATE_RANGE;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.LEGAL_DOCUMENT_EXPIRATION_ALREADY_SET;
import static net.akive.api.legal.domain.LegalPolicy.DOCUMENT_VERSION_MAX_LENGTH;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class LegalDocument {

    private Long legalDocumentId;

    private LegalDocumentType documentType;

    private String version;

    private String title;

    private LegalInteractionType interactionType;

    private boolean optional;

    private String content;

    private LocalDate announcedOn;

    private LocalDate effectiveOn;

    private LocalDate expiresOn;

    private Instant createdAt;

    public static LegalDocument reconstruct(
            Long legalDocumentId,
            LegalDocumentType documentType,
            String version,
            String title,
            LegalInteractionType interactionType,
            boolean optional,
            String content,
            LocalDate announcedOn,
            LocalDate effectiveOn,
            LocalDate expiresOn,
            Instant createdAt
    ) {
        LegalDocument legalDocument = LegalDocument.builder()
                .legalDocumentId(requirePositive(legalDocumentId))
                .documentType(requireNonNull(documentType))
                .version(requireMaxLength(requireNotBlank(version), DOCUMENT_VERSION_MAX_LENGTH))
                .title(requireNotBlank(title))
                .interactionType(requireNonNull(interactionType))
                .optional(optional)
                .content(validateContent(content))
                .announcedOn(announcedOn)
                .effectiveOn(requireNonNull(effectiveOn))
                .expiresOn(expiresOn)
                .createdAt(requireNonNull(createdAt))
                .build();

        legalDocument.validateDateRange();

        return legalDocument;
    }

    public static LegalDocument create(
            LegalDocumentType documentType,
            String version,
            String title,
            LegalInteractionType interactionType,
            boolean optional,
            String content,
            LocalDate announcedOn,
            LocalDate effectiveOn,
            LocalDate expiresOn
    ) {
        LegalDocument legalDocument = LegalDocument.builder()
                .documentType(requireNonNull(documentType))
                .version(requireMaxLength(requireNotBlank(version), DOCUMENT_VERSION_MAX_LENGTH))
                .title(requireNotBlank(title))
                .interactionType(requireNonNull(interactionType))
                .optional(optional)
                .content(validateContent(content))
                .announcedOn(announcedOn)
                .effectiveOn(requireNonNull(effectiveOn))
                .expiresOn(expiresOn)
                .build();

        legalDocument.validateDateRange();

        return legalDocument;
    }

    public void expireOn(LocalDate targetExpiresOn) {
        if (this.expiresOn != null) {
            throw new BusinessRuleViolationException(LEGAL_DOCUMENT_EXPIRATION_ALREADY_SET);
        }

        LocalDate validatedExpiresOn = requireNonNull(targetExpiresOn);

        if (!validatedExpiresOn.isAfter(this.effectiveOn)) {
            throw new BusinessRuleViolationException(INVALID_LEGAL_DOCUMENT_DATE_RANGE);
        }

        this.expiresOn = validatedExpiresOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LegalDocument that = (LegalDocument) o;

        return this.legalDocumentId != null
                && Objects.equals(this.legalDocumentId, that.legalDocumentId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private void validateDateRange() {
        if (this.announcedOn != null && this.announcedOn.isAfter(this.effectiveOn)) {
            throw new BusinessRuleViolationException(INVALID_LEGAL_DOCUMENT_DATE_RANGE);
        }

        if (this.expiresOn != null && !this.expiresOn.isAfter(this.effectiveOn)) {
            throw new BusinessRuleViolationException(INVALID_LEGAL_DOCUMENT_DATE_RANGE);
        }
    }

    private static String validateContent(String content) {
        requireNotBlank(content);

        return content;
    }
}
