package net.akive.api.learner.application.provided.dto;

import net.akive.api.eventprocessing.domain.EventType;

import java.util.List;
import java.util.UUID;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireNotEmpty;
import static net.akive.api.common.domain.DomainValidator.requirePositive;
import static net.akive.api.learner.application.provided.dto.AnalysisMaterialRole.EVIDENCE;

public record LearnerInsightAnalysisCommand(
        UUID eventId,
        EventType eventType,
        Long userId,
        LearnerInsightEvidenceSourceData evidenceSource,
        List<LearnerInsightAnalysisMaterialData> materials
) {

    public LearnerInsightAnalysisCommand {
        requireNonNull(eventId);
        requireNonNull(eventType);
        requirePositive(userId);
        requireNonNull(evidenceSource);
        materials = List.copyOf(requireNotEmpty(materials));

        requireNotEmpty(materials.stream()
                .filter(material -> material.role() == EVIDENCE)
                .toList());
    }
}
