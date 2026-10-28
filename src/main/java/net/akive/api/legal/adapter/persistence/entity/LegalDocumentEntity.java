package net.akive.api.legal.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentType;
import net.akive.api.legal.domain.LegalInteractionType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
        name = "legal_document",
        uniqueConstraints = @UniqueConstraint(
                name = "ux-legal_document-document_type-version",
                columnNames = {"document_type", "version"}
        )
)
@Getter
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class LegalDocumentEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long legalDocumentId;

    @Enumerated(STRING)
    @Column(nullable = false, updatable = false, columnDefinition = "VARCHAR(50)")
    private LegalDocumentType documentType;

    @Column(nullable = false, length = 30, updatable = false)
    private String version;

    @Column(nullable = false, updatable = false)
    private String title;

    @Enumerated(STRING)
    @Column(nullable = false, updatable = false, columnDefinition = "VARCHAR(50)")
    private LegalInteractionType interactionType;

    @Column(
            name = "is_optional",
            nullable = false,
            updatable = false,
            columnDefinition = "TINYINT(1) UNSIGNED NOT NULL"
    )
    private Byte optional;

    @Column(nullable = false, updatable = false, columnDefinition = "TEXT")
    private String content;

    @Column(updatable = false)
    private LocalDate announcedOn;

    @Column(nullable = false, updatable = false)
    private LocalDate effectiveOn;

    private LocalDate expiresOn;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static LegalDocumentEntity from(LegalDocument legalDocument) {
        return new LegalDocumentEntity(
                legalDocument.getLegalDocumentId(),
                legalDocument.getDocumentType(),
                legalDocument.getVersion(),
                legalDocument.getTitle(),
                legalDocument.getInteractionType(),
                optionalValue(legalDocument.isOptional()),
                legalDocument.getContent(),
                legalDocument.getAnnouncedOn(),
                legalDocument.getEffectiveOn(),
                legalDocument.getExpiresOn(),
                legalDocument.getCreatedAt()
        );
    }

    public LegalDocument to() {
        try {
            return LegalDocument.reconstruct(
                    this.legalDocumentId,
                    this.documentType,
                    this.version,
                    this.title,
                    this.interactionType,
                    this.isOptional(),
                    this.content,
                    this.announcedOn,
                    this.effectiveOn,
                    this.expiresOn,
                    this.createdAt
            );
        } catch (BusinessRuleViolationException e) {
            throw new InvalidPersistedDataException(
                    "Failed to reconstruct LegalDocument: " + this.legalDocumentId,
                    e
            );
        }
    }

    private boolean isOptional() {
        if (this.optional == null || (this.optional != 0 && this.optional != 1)) {
            throw new InvalidPersistedDataException("Invalid optional value: " + this.optional);
        }

        return this.optional == 1;
    }

    private static byte optionalValue(boolean optional) {
        return optional ? (byte) 1 : (byte) 0;
    }
}
