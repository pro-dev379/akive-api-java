package net.akive.api.eventprocessing.application.service;

import net.akive.api.eventprocessing.application.exception.ProducedEventNotFoundException;
import net.akive.api.eventprocessing.application.required.ProducedEventRepository;
import net.akive.api.eventprocessing.domain.EventType;
import net.akive.api.eventprocessing.domain.ProducedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static net.akive.api.eventprocessing.domain.EventType.COACHING_TURN_DONE;
import static net.akive.api.eventprocessing.domain.ProducedEventPolicy.*;
import static net.akive.api.eventprocessing.domain.ProducedEventStatus.*;
import static net.akive.api.eventprocessing.domain.fixture.ProducedEventFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ProducedEventClaimServiceTest {

    private static final Set<EventType> SUPPORTED_EVENT_TYPES = Set.of(COACHING_TURN_DONE);
    private static final Instant CLAIM_ATTEMPTED_AT = CREATED_AT.plus(PROCESSING_TIMEOUT).plusSeconds(1);
    private static final Instant PUBLISH_FAILED_AT = CLAIM_ATTEMPTED_AT.plusSeconds(1);
    private static final Instant NEXT_PUBLISH_ATTEMPT_AT = PUBLISH_FAILED_AT.plus(INITIAL_RETRY_DELAY);

    @Mock
    ProducedEventRepository producedEventRepository;

    @InjectMocks
    ProducedEventClaimService producedEventClaimService;

    @Test
    void 처리_시간이_초과된_이벤트를_재발행할_수_있도록_복구한다() {
        ProducedEvent timedOutEvent = reconstructProcessingProducedEvent(0);
        Instant processingStartedAtCutoff = CLAIM_ATTEMPTED_AT.minus(PROCESSING_TIMEOUT);
        givenTimedOutEvents(timedOutEvent);

        List<ProducedEvent> claimedEvents = producedEventClaimService.claim(
                SUPPORTED_EVENT_TYPES,
                CLAIM_ATTEMPTED_AT,
                PROCESSING_TIMEOUT,
                PUBLISH_ATTEMPT_MAX_COUNT,
                PUBLISH_BATCH_SIZE
        );

        assertThat(timedOutEvent.getStatus()).isEqualTo(PENDING);
        assertThat(timedOutEvent.getRetryCount()).isEqualTo(1);
        assertThat(timedOutEvent.getNextAttemptAt()).isEqualTo(CLAIM_ATTEMPTED_AT);
        assertThat(timedOutEvent.getProcessingStartedAt()).isNull();
        assertThat(claimedEvents).isEmpty();
        then(producedEventRepository).should().findTimedOutProcessingForUpdate(
                SUPPORTED_EVENT_TYPES,
                processingStartedAtCutoff,
                PUBLISH_BATCH_SIZE
        );
        then(producedEventRepository).should().save(timedOutEvent);
    }

    @Test
    void 메시지_큐에_발행할_대기_이벤트를_선점한다() {
        ProducedEvent pendingEvent = ProducedEvent.create(
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                CREATED_AT
        );
        givenPendingEvents(pendingEvent);

        List<ProducedEvent> claimedEvents = producedEventClaimService.claim(
                SUPPORTED_EVENT_TYPES,
                CLAIM_ATTEMPTED_AT,
                PROCESSING_TIMEOUT,
                PUBLISH_ATTEMPT_MAX_COUNT,
                PUBLISH_BATCH_SIZE
        );

        assertThat(pendingEvent.getStatus()).isEqualTo(PROCESSING);
        assertThat(pendingEvent.getProcessingStartedAt()).isEqualTo(CLAIM_ATTEMPTED_AT);
        assertThat(claimedEvents).containsExactly(pendingEvent);
        then(producedEventRepository).should().findPendingForUpdate(
                SUPPORTED_EVENT_TYPES,
                CLAIM_ATTEMPTED_AT,
                PUBLISH_BATCH_SIZE
        );
        then(producedEventRepository).should().save(pendingEvent);
    }

    @Test
    void 처리_시간이_초과된_이벤트가_최대_발행_시도_횟수에_도달하면_실패_처리한다() {
        ProducedEvent timedOutEvent = reconstructProcessingProducedEvent(PUBLISH_ATTEMPT_MAX_COUNT - 1);
        givenTimedOutEvents(timedOutEvent);

        producedEventClaimService.claim(
                SUPPORTED_EVENT_TYPES,
                CLAIM_ATTEMPTED_AT,
                PROCESSING_TIMEOUT,
                PUBLISH_ATTEMPT_MAX_COUNT,
                PUBLISH_BATCH_SIZE
        );

        assertThat(timedOutEvent.getStatus()).isEqualTo(FAILED);
        assertThat(timedOutEvent.getRetryCount()).isEqualTo(PUBLISH_ATTEMPT_MAX_COUNT);
        then(producedEventRepository).should().save(timedOutEvent);
    }

    @Test
    void 메시지_큐에_이벤트를_발행하면_발행_완료_상태로_변경한다() {
        ProducedEvent processingEvent = givenProcessingEvent(RETRY_COUNT);

        producedEventClaimService.markPublished(PRODUCED_EVENT_ID, PUBLISHED_AT);

        assertThat(processingEvent.getStatus()).isEqualTo(PUBLISHED);
        assertThat(processingEvent.getPublishedAt()).isEqualTo(PUBLISHED_AT);
        then(producedEventRepository).should().save(processingEvent);
    }

    @Test
    void 메시지_큐에_이벤트를_발행하지_못하면_다음_발행_시도를_예약한다() {
        ProducedEvent processingEvent = givenProcessingEvent(RETRY_COUNT);

        producedEventClaimService.handlePublishFailure(
                PRODUCED_EVENT_ID,
                PUBLISH_FAILED_AT,
                NEXT_PUBLISH_ATTEMPT_AT,
                PUBLISH_ATTEMPT_MAX_COUNT
        );

        assertThat(processingEvent.getStatus()).isEqualTo(PENDING);
        assertThat(processingEvent.getRetryCount()).isEqualTo(RETRY_COUNT + 1);
        assertThat(processingEvent.getNextAttemptAt()).isEqualTo(NEXT_PUBLISH_ATTEMPT_AT);
        then(producedEventRepository).should().save(processingEvent);
    }

    @Test
    void 다음_발행_시도_시각이_실패_시각보다_빠르면_실패_시각으로_예약한다() {
        ProducedEvent processingEvent = givenProcessingEvent(RETRY_COUNT);

        producedEventClaimService.handlePublishFailure(
                PRODUCED_EVENT_ID,
                PUBLISH_FAILED_AT,
                PUBLISH_FAILED_AT.minusSeconds(1),
                PUBLISH_ATTEMPT_MAX_COUNT
        );

        assertThat(processingEvent.getNextAttemptAt()).isEqualTo(PUBLISH_FAILED_AT);
    }

    @Test
    void 이벤트_발행_실패로_최대_시도_횟수에_도달하면_실패_상태로_변경한다() {
        ProducedEvent processingEvent = givenProcessingEvent(PUBLISH_ATTEMPT_MAX_COUNT - 1);

        producedEventClaimService.handlePublishFailure(
                PRODUCED_EVENT_ID,
                PUBLISH_FAILED_AT,
                NEXT_PUBLISH_ATTEMPT_AT,
                PUBLISH_ATTEMPT_MAX_COUNT
        );

        assertThat(processingEvent.getStatus()).isEqualTo(FAILED);
        assertThat(processingEvent.getRetryCount()).isEqualTo(PUBLISH_ATTEMPT_MAX_COUNT);
        then(producedEventRepository).should().save(processingEvent);
    }

    @Test
    void 메시지_큐에_발행할_이벤트를_찾을_수_없으면_예외를_던진다() {
        given(producedEventRepository.findByIdForUpdate(PRODUCED_EVENT_ID))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ProducedEventNotFoundException.class)
                .isThrownBy(() -> producedEventClaimService.markPublished(
                        PRODUCED_EVENT_ID,
                        PUBLISHED_AT
                ))
                .withMessage("Produced event could not be found: " + PRODUCED_EVENT_ID);
    }

    private void givenTimedOutEvents(ProducedEvent... timedOutEvents) {
        given(producedEventRepository.findTimedOutProcessingForUpdate(
                SUPPORTED_EVENT_TYPES,
                CLAIM_ATTEMPTED_AT.minus(PROCESSING_TIMEOUT),
                PUBLISH_BATCH_SIZE
        )).willReturn(List.of(timedOutEvents));
    }

    private void givenPendingEvents(ProducedEvent... pendingEvents) {
        given(producedEventRepository.findPendingForUpdate(
                SUPPORTED_EVENT_TYPES,
                CLAIM_ATTEMPTED_AT,
                PUBLISH_BATCH_SIZE
        )).willReturn(List.of(pendingEvents));
    }

    private ProducedEvent givenProcessingEvent(int retryCount) {
        ProducedEvent processingEvent = reconstructProcessingProducedEvent(retryCount);
        given(producedEventRepository.findByIdForUpdate(PRODUCED_EVENT_ID))
                .willReturn(Optional.of(processingEvent));

        return processingEvent;
    }
}
