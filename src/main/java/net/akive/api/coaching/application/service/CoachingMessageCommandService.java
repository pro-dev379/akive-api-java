package net.akive.api.coaching.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.akive.api.coaching.application.prompt.CoachingPrompt;
import net.akive.api.coaching.application.prompt.CoachingPromptComposition;
import net.akive.api.coaching.application.prompt.CoachingPromptFactory;
import net.akive.api.coaching.application.provided.CoachingMessageSender;
import net.akive.api.coaching.application.provided.dto.CoachingMessageSendCommand;
import net.akive.api.coaching.application.provided.dto.CoachingMessageSendResult;
import net.akive.api.coaching.application.required.CoachingConversationLock;
import net.akive.api.coaching.application.required.CoachingConversationLock.Lease;
import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.application.required.CoachingLlm;
import net.akive.api.coaching.application.required.CoachingContextProvider;
import net.akive.api.coaching.application.required.CoachingResponseRecoveryStore;
import net.akive.api.coaching.application.required.data.CoachingTurnRecoveryData;
import net.akive.api.coaching.application.stream.CoachingResponseStream;
import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.coaching.domain.CoachingMessage;
import net.akive.api.coaching.domain.MessageCompletionStatus;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.id.UuidGenerator;
import net.akive.api.llmusage.application.provided.LlmUsageQuotaChecker;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static net.akive.api.coaching.domain.MessageCompletionStatus.TRUNCATED;
import static net.akive.api.common.exception.business.ResourceConflictError.COACHING_RESPONSE_ALREADY_IN_PROGRESS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.COACHING_CONVERSATION_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
class CoachingMessageCommandService implements CoachingMessageSender {

    private final LlmUsageQuotaChecker llmUsageQuotaChecker;

    private final CoachingLlm coachingLlm;
    private final CoachingConversationRepository coachingConversationRepository;
    private final CoachingConversationLock coachingConversationLock;
    private final CoachingResponseRecoveryStore coachingResponseRecoveryStore;
    private final CoachingContextProvider coachingContextProvider;

    private final UuidGenerator uuidGenerator;
    private final CoachingTurnCompletionService coachingTurnCompletionService;
    private final CoachingPromptFactory coachingPromptFactory;

    @Override
    public CoachingMessageSendResult send(CoachingMessageSendCommand command)
            throws ResourceNotFoundException, ResourceConflictException {
        Lease lease = acquireLeaseOrThrow(command.coachingConversationId(), command.userId());

        CoachingConversation coachingConversation;
        try {
            coachingConversation = findCoachingConversationOrThrow(command);
            llmUsageQuotaChecker.checkUserDailyQuota(command.userId());
            recoverPendingResponse(coachingConversation);
        } catch (RuntimeException e) {
            lease.release();
            throw e;
        }

        CompletionStage<Optional<String>> conversationTitle =
                resolveConversationTitle(coachingConversation, command.userId(), command.content());

        try {
            CoachingPrompt coachingPrompt = coachingPromptFactory.createCoaching(createPromptComposition(command));

            CoachingMessage userMessage = coachingConversationRepository.saveMessage(
                    CoachingMessage.fromUser(coachingConversation, command.content())
            );

            CoachingResponseStream coachingResponseStream = coachingLlm
                    .streamResponse(command.userId(), coachingConversation.getLlmConversationId(), coachingPrompt)
                    .onResponse((content, completionStatus) -> {
                        saveResponseIfPresent(coachingConversation, userMessage, content, completionStatus);
                        conversationTitle.toCompletableFuture().join();
                    })
                    .onTermination(() -> releaseAfterTitleCompletion(conversationTitle, lease));

            return new CoachingMessageSendResult(coachingResponseStream, conversationTitle);
        } catch (RuntimeException e) {
            releaseAfterTitleCompletion(conversationTitle, lease);
            throw e;
        }
    }

    private CoachingPromptComposition createPromptComposition(CoachingMessageSendCommand command) {
        List<CoachingPromptComposition.ContextSection> contextSections = coachingContextProvider.findContextSections(
                command.userId(),
                command.content()
        );

        return contextSections.isEmpty()
                ? CoachingPromptComposition.from(command.content())
                : new CoachingPromptComposition(command.content(), contextSections);
    }

    private Lease acquireLeaseOrThrow(Long coachingConversationId, Long userId) {
        return coachingConversationLock.tryAcquire(coachingConversationId, userId)
                .orElseThrow(() -> new ResourceConflictException(COACHING_RESPONSE_ALREADY_IN_PROGRESS));
    }

    private CoachingConversation findCoachingConversationOrThrow(CoachingMessageSendCommand command) {
        return coachingConversationRepository
                .findByCoachingConversationIdAndUserId(command.coachingConversationId(), command.userId())
                .orElseThrow(() -> new ResourceNotFoundException(COACHING_CONVERSATION_NOT_FOUND));
    }

    private CompletionStage<Optional<String>> resolveConversationTitle(
            CoachingConversation coachingConversation,
            Long userId,
            String content
    ) {
        if (coachingConversation.getTitle() != null) {
            return CompletableFuture.completedFuture(Optional.of(coachingConversation.getTitle()));
        }

        try {
            return generateConversationTitle(userId, content)
                    .thenApply(conversationTitle -> saveConversationTitle(coachingConversation, conversationTitle))
                    .thenApply(Optional::of)
                    .exceptionally(error -> handleTitleGenerationFailure(coachingConversation, error));
        } catch (RuntimeException e) {
            return CompletableFuture.completedFuture(
                    handleTitleGenerationFailure(coachingConversation, e)
            );
        }
    }

    private CompletionStage<String> generateConversationTitle(Long userId, String content) {
        CoachingPrompt conversationTitlePrompt = coachingPromptFactory.createConversationTitle(content);

        return coachingLlm.generateConversationTitle(userId, conversationTitlePrompt);
    }

    private String saveConversationTitle(CoachingConversation coachingConversation, String title) {
        coachingConversation.updateTitle(title);
        coachingConversationRepository.save(coachingConversation);

        return title;
    }

    private Optional<String> handleTitleGenerationFailure(
            CoachingConversation coachingConversation,
            Throwable error
    ) {
        log.warn(
                "Failed to generate a coaching conversation conversationTitle: coachingConversationId={}",
                coachingConversation.getCoachingConversationId(),
                error
        );

        return Optional.empty();
    }

    private void releaseAfterTitleCompletion(CompletionStage<Optional<String>> conversationTitle, Lease lease) {
        conversationTitle.whenComplete((title, error) -> lease.release());
    }

    private void saveResponseIfPresent(
            CoachingConversation coachingConversation,
            CoachingMessage userMessage,
            String content,
            MessageCompletionStatus completionStatus
    ) {
        if (completionStatus == TRUNCATED && content.isBlank()) {
            return;
        }

        saveResponse(
                coachingConversation,
                new CoachingTurnRecoveryData(
                        uuidGenerator.generate(),
                        userMessage.getCoachingMessageId(),
                        content,
                        completionStatus
                )
        );
    }

    private void saveResponse(
            CoachingConversation coachingConversation,
            CoachingTurnRecoveryData recoveryData
    ) {
        Long coachingConversationId = coachingConversation.getCoachingConversationId();

        try {
            coachingResponseRecoveryStore.save(coachingConversationId, recoveryData);
        } catch (RuntimeException recoveryStoreException) {
            saveWithoutRecoveryMarker(coachingConversation, recoveryData, recoveryStoreException);

            return;
        }

        coachingTurnCompletionService.complete(coachingConversation, recoveryData);
        coachingResponseRecoveryStore.delete(coachingConversationId);
    }

    private void saveWithoutRecoveryMarker(
            CoachingConversation coachingConversation,
            CoachingTurnRecoveryData recoveryData,
            RuntimeException recoveryStoreException
    ) {
        try {
            coachingTurnCompletionService.complete(coachingConversation, recoveryData);

            log.warn(
                    "Saved coaching response without a recovery marker: coachingConversationId={}",
                    coachingConversation.getCoachingConversationId(),
                    recoveryStoreException
            );
        } catch (RuntimeException persistenceException) {
            persistenceException.addSuppressed(recoveryStoreException);

            throw persistenceException;
        }
    }

    private void recoverPendingResponse(CoachingConversation coachingConversation) {
        Long coachingConversationId = coachingConversation.getCoachingConversationId();

        coachingResponseRecoveryStore.find(coachingConversationId).ifPresent(recoveryData -> {
            coachingTurnCompletionService.complete(coachingConversation, recoveryData);
            coachingResponseRecoveryStore.delete(coachingConversationId);
        });
    }
}
