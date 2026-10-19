package net.akive.api.coaching.application.service;

import net.akive.api.coaching.application.prompt.CoachingPrompt;
import net.akive.api.coaching.application.prompt.CoachingPromptComposition;
import net.akive.api.coaching.application.prompt.CoachingPromptFactory;
import net.akive.api.coaching.application.provided.dto.CoachingMessageSendCommand;
import net.akive.api.coaching.application.provided.dto.CoachingMessageSendResult;
import net.akive.api.coaching.application.required.*;
import net.akive.api.coaching.application.required.data.CoachingTurnRecoveryData;
import net.akive.api.coaching.application.stream.CoachingResponseStream;
import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.coaching.domain.CoachingMessage;
import net.akive.api.common.exception.business.RateLimitExceededException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.id.UuidGenerator;
import net.akive.api.llmusage.application.provided.LlmUsageQuotaChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.*;
import static net.akive.api.coaching.application.prompt.fixture.CoachingPromptCompositionFixture.createContextSection;
import static net.akive.api.coaching.application.stream.CoachingResponseStream.IncompleteReason.MAX_OUTPUT_TOKENS;
import static net.akive.api.coaching.domain.ConversationContext.MAIN;
import static net.akive.api.coaching.domain.MessageCompletionStatus.COMPLETED;
import static net.akive.api.coaching.domain.MessageCompletionStatus.TRUNCATED;
import static net.akive.api.coaching.domain.MessageRole.USER;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.*;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.*;
import static net.akive.api.common.exception.business.ResourceConflictError.COACHING_RESPONSE_ALREADY_IN_PROGRESS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.COACHING_CONVERSATION_NOT_FOUND;
import static net.akive.api.common.exception.business.RateLimitError.LLM_DAILY_TOKEN_QUOTA_EXCEEDED;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CoachingMessageCommandServiceTest {

    private final CoachingPromptFactory coachingPromptFactory = new CoachingPromptFactory();

    @Mock
    LlmUsageQuotaChecker llmUsageQuotaChecker;

    @Mock
    CoachingLlm coachingLlm;

    @Mock
    CoachingConversationRepository coachingConversationRepository;

    @Mock
    CoachingConversationLock coachingConversationLock;

    @Mock
    CoachingResponseRecoveryStore coachingResponseRecoveryStore;

    @Mock
    CoachingContextProvider coachingContextProvider;

    @Mock
    UuidGenerator uuidGenerator;

    @Mock
    CoachingTurnCompletionService coachingTurnCompletionService;

    @Mock
    CoachingResponseStream.Listener downstreamListener;

    TestLease lease;

    CoachingMessageCommandService coachingMessageCommandService;

    @BeforeEach
    void setUp() {
        lease = new TestLease();
        coachingMessageCommandService = new CoachingMessageCommandService(
                llmUsageQuotaChecker,
                coachingLlm,
                coachingConversationRepository,
                coachingConversationLock,
                coachingResponseRecoveryStore,
                coachingContextProvider,
                uuidGenerator,
                coachingTurnCompletionService,
                coachingPromptFactory
        );
    }

    @Test
    void 코칭_대화_제목과_코칭_응답_생성을_병렬로_시작한다() {
        CoachingMessageSendCommand command = createCoachingMessageSendCommand();
        CoachingConversation coachingConversation = coachingConversationWithoutTitle();
        String generatedTitle = "학습 계획 코칭";
        CompletableFuture<String> conversationTitle = new CompletableFuture<>();
        given(coachingLlm.generateConversationTitle(eq(command.userId()), any(CoachingPrompt.class)))
                .willReturn(conversationTitle);
        prepareMessageSend(command, coachingConversation);

        CoachingMessageSendResult result = coachingMessageCommandService.send(command);

        assertThat(result.conversationTitle().toCompletableFuture()).isNotDone();
        then(coachingLlm).should().streamResponse(
                eq(command.userId()),
                eq(coachingConversation.getLlmConversationId()),
                any(CoachingPrompt.class)
        );

        conversationTitle.complete(generatedTitle);

        assertThat(result.conversationTitle().toCompletableFuture().join()).contains(generatedTitle);
        assertThat(coachingConversation.getTitle()).isEqualTo(generatedTitle);
        then(coachingConversationRepository).should().save(coachingConversation);
    }

    @Test
    void 기존_코칭_대화_제목이_있으면_새_제목을_생성하지_않고_기존_제목을_반환한다() {
        CoachingMessageSendCommand command = createCoachingMessageSendCommand();
        CoachingConversation coachingConversation = reconstructCoachingConversation();
        String existingTitle = coachingConversation.getTitle();
        prepareMessageSend(command, coachingConversation);

        CoachingMessageSendResult result = coachingMessageCommandService.send(command);

        assertThat(result.conversationTitle().toCompletableFuture().join()).contains(existingTitle);
        then(coachingLlm).should(never()).generateConversationTitle(
                eq(command.userId()),
                any(CoachingPrompt.class)
        );
    }

    @Test
    void 코칭_대화_락을_획득한_후_코칭_대화를_조회하고_AI_코치_응답_스트림을_생성한다() {
        CoachingMessageSendCommand command = createCoachingMessageSendCommand();
        CoachingConversation coachingConversation = reconstructCoachingConversation();
        prepareMessageSend(command, coachingConversation);

        coachingMessageCommandService.send(command);

        InOrder requestOrder = inOrder(
                coachingConversationLock,
                coachingConversationRepository,
                coachingLlm
        );
        requestOrder.verify(coachingConversationLock).tryAcquire(
                command.coachingConversationId(),
                command.userId()
        );
        requestOrder.verify(coachingConversationRepository).findByCoachingConversationIdAndUserId(
                command.coachingConversationId(),
                command.userId()
        );
        requestOrder.verify(coachingLlm).streamResponse(
                eq(command.userId()),
                eq(coachingConversation.getLlmConversationId()),
                any(CoachingPrompt.class)
        );
    }

    @Test
    void 코칭_대화_제목_생성에_실패하면_제목을_반환하지_않는다() {
        CoachingMessageSendCommand command = createCoachingMessageSendCommand();
        CoachingConversation coachingConversation = coachingConversationWithoutTitle();
        CompletableFuture<String> conversationTitle = new CompletableFuture<>();
        given(coachingLlm.generateConversationTitle(eq(command.userId()), any(CoachingPrompt.class)))
                .willReturn(conversationTitle);
        prepareMessageSend(command, coachingConversation);

        CoachingMessageSendResult result = coachingMessageCommandService.send(command);
        conversationTitle.completeExceptionally(new IllegalStateException("Title generation failed."));

        assertThat(result.conversationTitle().toCompletableFuture().join()).isEmpty();
        assertThat(coachingConversation.getTitle()).isNull();
        then(coachingConversationRepository).should(never()).save(coachingConversation);
    }

    @Test
    void 회원_메시지와_학습자_컨텍스트를_코칭_LLM에_전달한다() {
        CoachingMessageSendCommand command = createCoachingMessageSendCommand();
        CoachingConversation coachingConversation = reconstructCoachingConversation();
        CoachingPromptComposition.ContextSection contextSection = createContextSection();
        prepareMessageSend(command, coachingConversation, List.of(contextSection));

        coachingMessageCommandService.send(command);

        ArgumentCaptor<CoachingMessage> userMessageCaptor = ArgumentCaptor.forClass(CoachingMessage.class);
        then(coachingConversationRepository).should().saveCoachingMessage(userMessageCaptor.capture());

        CoachingMessage userMessage = userMessageCaptor.getValue();
        assertThat(userMessage.getCoachingConversation()).isEqualTo(coachingConversation);
        assertThat(userMessage.getRole()).isEqualTo(USER);
        assertThat(userMessage.getContent()).isEqualTo(command.content());
        assertThat(userMessage.getCompletionStatus()).isEqualTo(COMPLETED);

        ArgumentCaptor<CoachingPrompt> coachingPromptCaptor = ArgumentCaptor.forClass(CoachingPrompt.class);
        then(coachingLlm).should().streamResponse(
                eq(command.userId()),
                eq(coachingConversation.getLlmConversationId()),
                coachingPromptCaptor.capture()
        );
        assertThat(coachingPromptCaptor.getValue().input()).contains(
                command.content(),
                contextSection.name(),
                contextSection.content()
        );
    }

    @Test
    void 코칭_대화를_조회할_수_없으면_메시지를_전송할_수_없다() {
        CoachingMessageSendCommand command = createCoachingMessageSendCommand();
        given(coachingConversationLock.tryAcquire(command.coachingConversationId(), command.userId()))
                .willReturn(Optional.of(lease));
        given(coachingConversationRepository.findByCoachingConversationIdAndUserId(
                command.coachingConversationId(),
                command.userId()
        )).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> coachingMessageCommandService.send(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(COACHING_CONVERSATION_NOT_FOUND);

        assertThat(lease.isReleased()).isTrue();
        then(llmUsageQuotaChecker).shouldHaveNoInteractions();
        then(coachingLlm).shouldHaveNoInteractions();
    }

    @Test
    void AI_코치_응답_메시지_생성_중에는_회원이_메시지를_전송할_수_없다() {
        CoachingMessageSendCommand command = createCoachingMessageSendCommand();
        given(coachingConversationLock.tryAcquire(command.coachingConversationId(), command.userId()))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> coachingMessageCommandService.send(command))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(COACHING_RESPONSE_ALREADY_IN_PROGRESS);

        then(llmUsageQuotaChecker).shouldHaveNoInteractions();
        then(coachingConversationRepository).shouldHaveNoInteractions();
        then(coachingResponseRecoveryStore).shouldHaveNoInteractions();
        then(coachingLlm).shouldHaveNoInteractions();
        then(coachingConversationRepository).should(never()).saveCoachingMessage(any());
    }

    @Test
    void LLM_일일_토큰_한도를_초과하면_락을_해제한다() {
        CoachingMessageSendCommand command = createCoachingMessageSendCommand();
        CoachingConversation coachingConversation = reconstructCoachingConversation();
        given(coachingConversationLock.tryAcquire(command.coachingConversationId(), command.userId()))
                .willReturn(Optional.of(lease));
        given(coachingConversationRepository.findByCoachingConversationIdAndUserId(
                command.coachingConversationId(),
                command.userId()
        )).willReturn(Optional.of(coachingConversation));
        willThrow(new RateLimitExceededException(LLM_DAILY_TOKEN_QUOTA_EXCEEDED))
                .given(llmUsageQuotaChecker)
                .checkUserDailyQuota(command.userId());

        assertThatExceptionOfType(RateLimitExceededException.class)
                .isThrownBy(() -> coachingMessageCommandService.send(command))
                .extracting(RateLimitExceededException::getError)
                .isEqualTo(LLM_DAILY_TOKEN_QUOTA_EXCEEDED);

        assertThat(lease.isReleased()).isTrue();
        then(coachingResponseRecoveryStore).shouldHaveNoInteractions();
        then(coachingLlm).shouldHaveNoInteractions();
    }

    @Test
    void 저장되지_않은_이전_AI_코치_응답_메시지를_복구한_후_메시지를_전송한다() {
        CoachingMessageSendCommand command = createCoachingMessageSendCommand();
        CoachingConversation coachingConversation = reconstructCoachingConversation();
        CoachingTurnRecoveryData recoveryData = createCoachingTurnRecoveryData();
        prepareMessageSendWithRecovery(command, coachingConversation, recoveryData);

        coachingMessageCommandService.send(command);

        InOrder recoveryOrder = inOrder(
                coachingTurnCompletionService,
                coachingResponseRecoveryStore,
                coachingConversationRepository,
                coachingLlm
        );
        recoveryOrder.verify(coachingTurnCompletionService).complete(coachingConversation, recoveryData);
        recoveryOrder.verify(coachingResponseRecoveryStore).delete(command.coachingConversationId());
        recoveryOrder.verify(coachingConversationRepository).saveCoachingMessage(any(CoachingMessage.class));
        recoveryOrder.verify(coachingLlm).streamResponse(
                eq(command.userId()),
                eq(coachingConversation.getLlmConversationId()),
                any(CoachingPrompt.class)
        );
    }

    @Test
    void 저장되지_않은_이전_AI_코치_응답_메시지를_복구할_수_없으면_락을_해제한다() {
        CoachingMessageSendCommand command = createCoachingMessageSendCommand();
        CoachingConversation coachingConversation = reconstructCoachingConversation();
        CoachingTurnRecoveryData recoveryData = createCoachingTurnRecoveryData();
        IllegalStateException recoveryException = new IllegalStateException("Recovery failed.");
        given(coachingConversationRepository.findByCoachingConversationIdAndUserId(
                command.coachingConversationId(),
                command.userId()
        )).willReturn(Optional.of(coachingConversation));
        given(coachingConversationLock.tryAcquire(command.coachingConversationId(), command.userId()))
                .willReturn(Optional.of(lease));
        given(coachingResponseRecoveryStore.find(command.coachingConversationId()))
                .willReturn(Optional.of(recoveryData));
        willThrow(recoveryException)
                .given(coachingTurnCompletionService)
                .complete(coachingConversation, recoveryData);

        assertThatThrownBy(() -> coachingMessageCommandService.send(command))
                .isSameAs(recoveryException);

        assertThat(lease.isReleased()).isTrue();
        then(coachingResponseRecoveryStore).should(never()).delete(command.coachingConversationId());
        then(coachingLlm).shouldHaveNoInteractions();
        then(coachingConversationRepository).should(never()).saveCoachingMessage(any());
    }

    @Test
    void 완료된_AI_코치_응답_메시지를_임시_보관한_후_DB에_저장하고_락을_해제한다() {
        CoachingMessageSendCommand command = createCoachingMessageSendCommand();
        CoachingConversation coachingConversation = reconstructCoachingConversation();
        PreparedMessageSend preparedMessageSend = prepareMessageSend(command, coachingConversation);
        given(uuidGenerator.generate()).willReturn(EVENT_ID);

        CoachingMessageSendResult result = coachingMessageCommandService.send(command);
        result.responseStream().subscribe(downstreamListener);
        assertThat(lease.isReleased()).isFalse();

        preparedMessageSend.responseStream().complete(COACHING_RESPONSE_CONTENT);

        ArgumentCaptor<CoachingTurnRecoveryData> recoveryDataCaptor =
                ArgumentCaptor.forClass(CoachingTurnRecoveryData.class);
        InOrder persistenceOrder = inOrder(coachingResponseRecoveryStore, coachingTurnCompletionService);
        persistenceOrder.verify(coachingResponseRecoveryStore)
                .save(eq(command.coachingConversationId()), recoveryDataCaptor.capture());

        CoachingTurnRecoveryData recoveryData = recoveryDataCaptor.getValue();
        assertThat(recoveryData.eventId()).isEqualTo(EVENT_ID);
        assertThat(recoveryData.userMessageId())
                .isEqualTo(preparedMessageSend.savedUserMessage().getCoachingMessageId());
        assertThat(recoveryData.content()).isEqualTo(COACHING_RESPONSE_CONTENT);
        assertThat(recoveryData.completionStatus()).isEqualTo(COMPLETED);

        persistenceOrder.verify(coachingTurnCompletionService).complete(coachingConversation, recoveryData);
        persistenceOrder.verify(coachingResponseRecoveryStore).delete(command.coachingConversationId());
        assertThat(lease.isReleased()).isTrue();
        then(downstreamListener).should().onComplete();
    }

    @Test
    void 완료된_AI_코치_응답_메시지를_DB에_저장할_수_없으면_임시_보관된_응답을_유지하고_오류를_전달한다() {
        CoachingMessageSendCommand command = createCoachingMessageSendCommand();
        CoachingConversation coachingConversation = reconstructCoachingConversation();
        TestCoachingResponseStream responseStream = prepareMessageSend(command, coachingConversation).responseStream();
        IllegalStateException persistenceException = new IllegalStateException("Response persistence failed.");
        given(uuidGenerator.generate()).willReturn(EVENT_ID);
        willThrow(persistenceException)
                .given(coachingTurnCompletionService)
                .complete(eq(coachingConversation), any(CoachingTurnRecoveryData.class));

        CoachingMessageSendResult result = coachingMessageCommandService.send(command);
        result.responseStream().subscribe(downstreamListener);
        responseStream.complete(COACHING_RESPONSE_CONTENT);

        then(coachingResponseRecoveryStore).should().save(
                eq(command.coachingConversationId()),
                any(CoachingTurnRecoveryData.class)
        );
        then(coachingResponseRecoveryStore).should(never()).delete(command.coachingConversationId());
        assertThat(lease.releaseCount()).isOne();
        then(downstreamListener).should().onError(persistenceException);
        then(downstreamListener).should(never()).onComplete();
    }

    @Test
    void AI_코치_응답_메시지_생성이_중단되어_메시지_내용이_잘리면_잘린_상태로_저장하고_락을_해제한다() {
        CoachingMessageSendCommand command = createCoachingMessageSendCommand();
        CoachingConversation coachingConversation = reconstructCoachingConversation();
        String truncatedResponseContent = "우선 계획을 작은 단위로";
        TestCoachingResponseStream responseStream = prepareMessageSend(command, coachingConversation).responseStream();
        given(uuidGenerator.generate()).willReturn(EVENT_ID);

        CoachingMessageSendResult result = coachingMessageCommandService.send(command);
        result.responseStream().subscribe(downstreamListener);
        responseStream.incomplete(truncatedResponseContent);

        ArgumentCaptor<CoachingTurnRecoveryData> recoveryDataCaptor =
                ArgumentCaptor.forClass(CoachingTurnRecoveryData.class);
        then(coachingResponseRecoveryStore).should().save(
                eq(command.coachingConversationId()),
                recoveryDataCaptor.capture()
        );

        CoachingTurnRecoveryData recoveryData = recoveryDataCaptor.getValue();
        assertThat(recoveryData.eventId()).isEqualTo(EVENT_ID);
        assertThat(recoveryData.content()).isEqualTo(truncatedResponseContent);
        assertThat(recoveryData.completionStatus()).isEqualTo(TRUNCATED);
        then(coachingTurnCompletionService).should().complete(coachingConversation, recoveryData);
        then(coachingResponseRecoveryStore).should().delete(command.coachingConversationId());
        assertThat(lease.releaseCount()).isOne();
        then(downstreamListener).should().onIncomplete(MAX_OUTPUT_TOKENS);
        then(downstreamListener).should(never()).onComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n\t"})
    void AI_코치_응답_메시지_생성_도중_메시지_내용이_공백이면서_잘리면_메시지를_저장하지_않고_락을_해제한다(String blankContent) {
        CoachingMessageSendCommand command = createCoachingMessageSendCommand();
        CoachingConversation coachingConversation = reconstructCoachingConversation();
        TestCoachingResponseStream responseStream = prepareMessageSend(command, coachingConversation).responseStream();

        CoachingMessageSendResult result = coachingMessageCommandService.send(command);
        result.responseStream().subscribe(downstreamListener);
        responseStream.incomplete(blankContent);

        then(uuidGenerator).shouldHaveNoInteractions();
        then(coachingResponseRecoveryStore).should(never()).save(any(), any());
        then(coachingTurnCompletionService).shouldHaveNoInteractions();
        assertThat(lease.isReleased()).isTrue();
        then(downstreamListener).should().onIncomplete(MAX_OUTPUT_TOKENS);
    }

    @Test
    void AI_코치_응답_메시지를_임시_보관할_수_없어도_DB에_저장한다() {
        CoachingMessageSendCommand command = createCoachingMessageSendCommand();
        CoachingConversation coachingConversation = reconstructCoachingConversation();
        TestCoachingResponseStream responseStream = prepareMessageSend(command, coachingConversation).responseStream();
        IllegalStateException recoveryStoreException = new IllegalStateException("Recovery store failed.");
        given(uuidGenerator.generate()).willReturn(EVENT_ID);
        willThrow(recoveryStoreException)
                .given(coachingResponseRecoveryStore)
                .save(eq(command.coachingConversationId()), any(CoachingTurnRecoveryData.class));

        CoachingMessageSendResult result = coachingMessageCommandService.send(command);
        result.responseStream().subscribe(downstreamListener);
        responseStream.complete(COACHING_RESPONSE_CONTENT);

        then(coachingTurnCompletionService).should().complete(
                eq(coachingConversation),
                any(CoachingTurnRecoveryData.class)
        );
        then(coachingResponseRecoveryStore).should(never()).delete(command.coachingConversationId());
        assertThat(lease.isReleased()).isTrue();
        then(downstreamListener).should().onComplete();
    }

    @Test
    void AI_코치_응답_스트림을_생성할_수_없으면_제목_생성_완료_후_락을_한_번_해제한다() {
        CoachingMessageSendCommand command = createCoachingMessageSendCommand();
        CoachingConversation coachingConversation = coachingConversationWithoutTitle();
        String generatedTitle = "학습 계획 코칭";
        CompletableFuture<String> conversationTitle = new CompletableFuture<>();
        IllegalStateException streamException = new IllegalStateException("Stream creation failed.");
        given(coachingLlm.generateConversationTitle(eq(command.userId()), any(CoachingPrompt.class)))
                .willReturn(conversationTitle);
        prepareMessageSendDependencies(command, coachingConversation);
        given(coachingResponseRecoveryStore.find(command.coachingConversationId()))
                .willReturn(Optional.empty());
        given(coachingLlm.streamResponse(
                eq(command.userId()),
                eq(coachingConversation.getLlmConversationId()),
                any(CoachingPrompt.class)
        )).willThrow(streamException);

        assertThatThrownBy(() -> coachingMessageCommandService.send(command)).isSameAs(streamException);

        assertThat(lease.isReleased()).isFalse();
        assertThat(lease.releaseCount()).isZero();

        conversationTitle.complete(generatedTitle);

        assertThat(lease.isReleased()).isTrue();
        assertThat(lease.releaseCount()).isOne();
    }

    private PreparedMessageSend prepareMessageSend(
            CoachingMessageSendCommand command,
            CoachingConversation coachingConversation
    ) {
        return prepareMessageSend(command, coachingConversation, List.of());
    }

    private PreparedMessageSend prepareMessageSend(
            CoachingMessageSendCommand command,
            CoachingConversation coachingConversation,
            List<CoachingPromptComposition.ContextSection> contextSections
    ) {
        CoachingMessage savedUserMessage = prepareMessageSendDependencies(
                command,
                coachingConversation,
                contextSections
        );
        given(coachingResponseRecoveryStore.find(command.coachingConversationId()))
                .willReturn(Optional.empty());

        return prepareResponseStream(command, coachingConversation, savedUserMessage);
    }

    private PreparedMessageSend prepareMessageSendWithRecovery(
            CoachingMessageSendCommand command,
            CoachingConversation coachingConversation,
            CoachingTurnRecoveryData recoveryData
    ) {
        CoachingMessage savedUserMessage = prepareMessageSendDependencies(command, coachingConversation);
        given(coachingResponseRecoveryStore.find(command.coachingConversationId()))
                .willReturn(Optional.of(recoveryData));

        return prepareResponseStream(command, coachingConversation, savedUserMessage);
    }

    private PreparedMessageSend prepareResponseStream(
            CoachingMessageSendCommand command,
            CoachingConversation coachingConversation,
            CoachingMessage savedUserMessage
    ) {
        TestCoachingResponseStream responseStream = new TestCoachingResponseStream();
        given(coachingLlm.streamResponse(
                eq(command.userId()),
                eq(coachingConversation.getLlmConversationId()),
                any(CoachingPrompt.class)
        )).willReturn(responseStream);

        return new PreparedMessageSend(responseStream, savedUserMessage);
    }

    private CoachingMessage prepareMessageSendDependencies(
            CoachingMessageSendCommand command,
            CoachingConversation coachingConversation
    ) {
        return prepareMessageSendDependencies(command, coachingConversation, List.of());
    }

    private CoachingMessage prepareMessageSendDependencies(
            CoachingMessageSendCommand command,
            CoachingConversation coachingConversation,
            List<CoachingPromptComposition.ContextSection> contextSections
    ) {
        CoachingMessage savedUserMessage = reconstructCoachingMessage(
                COACHING_MESSAGE_ID,
                coachingConversation,
                USER,
                command.content(),
                MESSAGE_CREATED_AT
        );
        given(coachingContextProvider.findContextSections(command.userId(), command.content()))
                .willReturn(contextSections);
        given(coachingConversationRepository.findByCoachingConversationIdAndUserId(
                command.coachingConversationId(),
                command.userId()
        )).willReturn(Optional.of(coachingConversation));
        given(coachingConversationLock.tryAcquire(command.coachingConversationId(), command.userId()))
                .willReturn(Optional.of(lease));
        given(coachingConversationRepository.saveCoachingMessage(any(CoachingMessage.class)))
                .willReturn(savedUserMessage);

        return savedUserMessage;
    }

    private CoachingConversation coachingConversationWithoutTitle() {
        return reconstructCoachingConversation(
                COACHING_CONVERSATION_ID,
                createUser(),
                LLM_CONVERSATION_ID,
                null,
                MAIN
        );
    }

    private record PreparedMessageSend(
            TestCoachingResponseStream responseStream,
            CoachingMessage savedUserMessage
    ) {
    }

    private static final class TestLease implements CoachingConversationLock.Lease {

        private int releaseCount;

        @Override
        public void release() {
            releaseCount++;
        }

        private boolean isReleased() {
            return releaseCount > 0;
        }

        private int releaseCount() {
            return releaseCount;
        }
    }

    private static final class TestCoachingResponseStream implements CoachingResponseStream {

        private Listener listener;

        @Override
        public void subscribe(Listener downstreamListener) {
            this.listener = downstreamListener;
        }

        @Override
        public void cancel() {
        }

        private void complete(String content) {
            this.listener.onDelta(content);
            this.listener.onComplete();
        }

        private void incomplete(String content) {
            if (!content.isEmpty()) {
                this.listener.onDelta(content);
            }

            this.listener.onIncomplete(MAX_OUTPUT_TOKENS);
        }
    }
}
