package net.akive.api.eventprocessing.application.service;

import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.eventprocessing.application.required.IntegrationEventPublisher;
import net.akive.api.eventprocessing.domain.EventType;
import net.akive.api.eventprocessing.domain.ProducedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static net.akive.api.eventprocessing.domain.ProducedEventPolicy.*;
import static net.akive.api.eventprocessing.domain.fixture.ProducedEventFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ProducedEventPublishServiceTest {

    private static final Set<EventType> SUPPORTED_EVENT_TYPES = Set.of(EventType.values());
    private static final Instant PUBLISH_ATTEMPTED_AT = CREATED_AT.plusSeconds(10);
    private static final Instant PUBLISH_COMPLETED_AT = PUBLISH_ATTEMPTED_AT.plusSeconds(1);
    private static final Instant PUBLISH_FAILED_AT = PUBLISH_ATTEMPTED_AT.plusSeconds(2);

    @Mock
    IntegrationEventPublisher integrationEventPublisher;

    @Mock
    ProducedEventClaimService producedEventClaimService;

    @Mock
    CurrentTimeProvider currentTimeProvider;

    @InjectMocks
    ProducedEventPublishService producedEventPublishService;

    @Test
    void 대기_이벤트를_선점해서_메시지_큐에_발행하고_완료_처리한다() {
        ProducedEvent producedEvent = reconstructProcessingProducedEvent(RETRY_COUNT);
        givenClaimedEvents(producedEvent);
        given(currentTimeProvider.now()).willReturn(PUBLISH_ATTEMPTED_AT, PUBLISH_COMPLETED_AT);

        producedEventPublishService.publishPending();

        then(producedEventClaimService).should().claim(
                SUPPORTED_EVENT_TYPES,
                PUBLISH_ATTEMPTED_AT,
                PROCESSING_TIMEOUT,
                PUBLISH_ATTEMPT_MAX_COUNT,
                PUBLISH_BATCH_SIZE
        );
        InOrder publishOrder = inOrder(integrationEventPublisher, producedEventClaimService);
        publishOrder.verify(integrationEventPublisher).publish(producedEvent);
        publishOrder.verify(producedEventClaimService).markPublished(
                PRODUCED_EVENT_ID,
                PUBLISH_COMPLETED_AT
        );
    }

    @Test
    void 메시지_큐에_이벤트를_발행하지_못하면_지수_백오프로_다음_발행_시도를_예약한다() {
        ProducedEvent producedEvent = reconstructProcessingProducedEvent(RETRY_COUNT);
        Duration expectedRetryDelay = INITIAL_RETRY_DELAY.multipliedBy(1L << RETRY_COUNT);
        givenClaimedEvents(producedEvent);
        given(currentTimeProvider.now()).willReturn(PUBLISH_ATTEMPTED_AT, PUBLISH_FAILED_AT);
        willThrow(new RuntimeException("SQS is unavailable."))
                .given(integrationEventPublisher)
                .publish(producedEvent);

        producedEventPublishService.publishPending();

        then(producedEventClaimService).should().handlePublishFailure(
                PRODUCED_EVENT_ID,
                PUBLISH_FAILED_AT,
                PUBLISH_FAILED_AT.plus(expectedRetryDelay),
                PUBLISH_ATTEMPT_MAX_COUNT
        );
        then(producedEventClaimService).should(never()).markPublished(anyLong(), any(Instant.class));
    }

    @Test
    void 발행_완료_상태를_저장하지_못하면_지수_백오프로_다음_발행_시도를_예약한다() {
        ProducedEvent producedEvent = reconstructProcessingProducedEvent(RETRY_COUNT);
        Duration expectedRetryDelay = INITIAL_RETRY_DELAY.multipliedBy(1L << RETRY_COUNT);
        givenClaimedEvents(producedEvent);
        given(currentTimeProvider.now())
                .willReturn(PUBLISH_ATTEMPTED_AT, PUBLISH_COMPLETED_AT, PUBLISH_FAILED_AT);
        willThrow(new RuntimeException("Failed to update published status."))
                .given(producedEventClaimService)
                .markPublished(PRODUCED_EVENT_ID, PUBLISH_COMPLETED_AT);

        producedEventPublishService.publishPending();

        then(integrationEventPublisher).should().publish(producedEvent);
        then(producedEventClaimService).should().handlePublishFailure(
                PRODUCED_EVENT_ID,
                PUBLISH_FAILED_AT,
                PUBLISH_FAILED_AT.plus(expectedRetryDelay),
                PUBLISH_ATTEMPT_MAX_COUNT
        );
    }

    @Test
    void 발행_실패_상태도_저장하지_못하면_상태_저장_예외에_발행_예외를_포함해서_던진다() {
        ProducedEvent producedEvent = reconstructProcessingProducedEvent(RETRY_COUNT);
        RuntimeException publicationException = new RuntimeException("SQS is unavailable.");
        RuntimeException stateUpdateException = new RuntimeException("Failed to update failure status.");
        Duration expectedRetryDelay = INITIAL_RETRY_DELAY.multipliedBy(1L << RETRY_COUNT);
        givenClaimedEvents(producedEvent);
        given(currentTimeProvider.now()).willReturn(PUBLISH_ATTEMPTED_AT, PUBLISH_FAILED_AT);
        willThrow(publicationException).given(integrationEventPublisher).publish(producedEvent);
        willThrow(stateUpdateException).given(producedEventClaimService).handlePublishFailure(
                PRODUCED_EVENT_ID,
                PUBLISH_FAILED_AT,
                PUBLISH_FAILED_AT.plus(expectedRetryDelay),
                PUBLISH_ATTEMPT_MAX_COUNT
        );

        assertThatThrownBy(producedEventPublishService::publishPending).isSameAs(stateUpdateException);
        assertThat(stateUpdateException.getSuppressed()).containsExactly(publicationException);
    }

    private void givenClaimedEvents(ProducedEvent... producedEvents) {
        given(integrationEventPublisher.supportedEventTypes()).willReturn(SUPPORTED_EVENT_TYPES);
        given(producedEventClaimService.claim(
                SUPPORTED_EVENT_TYPES,
                PUBLISH_ATTEMPTED_AT,
                PROCESSING_TIMEOUT,
                PUBLISH_ATTEMPT_MAX_COUNT,
                PUBLISH_BATCH_SIZE
        )).willReturn(List.of(producedEvents));
    }
}
