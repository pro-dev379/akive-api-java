package net.akive.api.coaching.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.akive.api.coaching.application.provided.CoachingConversationCreator;
import net.akive.api.coaching.application.provided.dto.CoachingConversationCreateCommand;
import net.akive.api.coaching.application.required.CoachingConversationLock;
import net.akive.api.coaching.application.required.CoachingConversationLock.Lease;
import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.application.required.CoachingLlm;
import net.akive.api.coaching.application.required.CoachingResponseRecoveryStore;
import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.common.exception.business.AuthorizationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.legal.application.provided.LegalRequirementChecker;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;

import static net.akive.api.common.exception.business.AuthorizationError.AI_FEATURE_LEGAL_REQUIREMENTS_NOT_SATISFIED;
import static net.akive.api.common.exception.business.ResourceConflictError.COACHING_RESPONSE_ALREADY_IN_PROGRESS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.COACHING_CONVERSATION_NOT_FOUND;
import static net.akive.api.legal.domain.LegalRequirementScope.AI_FEATURE;

@Service
@Slf4j
@RequiredArgsConstructor
class CoachingConversationCommandService implements CoachingConversationCreator {

    private final UserFinder userFinder;
    private final LegalRequirementChecker legalRequirementChecker;

    private final CoachingLlm coachingLlm;
    private final CoachingConversationRepository coachingConversationRepository;
    private final CoachingConversationLock coachingConversationLock;
    private final CoachingResponseRecoveryStore coachingResponseRecoveryStore;

    @Override
    public Long create(CoachingConversationCreateCommand command) throws ResourceNotFoundException {
        User user = userFinder.findOrThrow(command.userId());
        validateAiFeatureRequirements(command.userId());
        String llmConversationId = coachingLlm.createConversation();

        return coachingConversationRepository
                .save(CoachingConversation.create(user, llmConversationId, command.context()))
                .getCoachingConversationId();
    }

    @Override
    public void delete(Long coachingConversationId, Long userId)
            throws ResourceNotFoundException, ResourceConflictException {
        Lease lease = coachingConversationLock.tryAcquire(coachingConversationId, userId)
                .orElseThrow(() -> new ResourceConflictException(COACHING_RESPONSE_ALREADY_IN_PROGRESS));

        try {
            CoachingConversation coachingConversation = coachingConversationRepository
                    .findByCoachingConversationIdAndUserId(coachingConversationId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException(COACHING_CONVERSATION_NOT_FOUND));

            coachingLlm.deleteConversation(coachingConversation.getLlmConversationId());
            coachingConversationRepository.delete(coachingConversation);
            deleteRecoveryDataSafely(coachingConversation.getCoachingConversationId());
        } finally {
            lease.release();
        }
    }

    private void validateAiFeatureRequirements(Long userId) {
        if (!legalRequirementChecker.check(userId, AI_FEATURE).satisfied()) {
            throw new AuthorizationException(AI_FEATURE_LEGAL_REQUIREMENTS_NOT_SATISFIED);
        }
    }

    private void deleteRecoveryDataSafely(Long coachingConversationId) {
        try {
            coachingResponseRecoveryStore.delete(coachingConversationId);
        } catch (RuntimeException e) {
            log.warn(
                    "Failed to delete coaching response recovery data: coachingConversationId={}",
                    coachingConversationId,
                    e
            );
        }
    }
}
