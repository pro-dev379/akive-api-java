package net.akive.api.eventprocessing.adapter.persistence;

import net.akive.api.eventprocessing.adapter.persistence.entity.ProducedEventEntity;
import net.akive.api.eventprocessing.application.required.ProducedEventRepository;
import net.akive.api.eventprocessing.domain.ProducedEvent;
import net.akive.api.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;
import static net.akive.api.eventprocessing.domain.EventType.COACHING_TURN_DONE;
import static net.akive.api.eventprocessing.domain.EventType.INSIGHT_CHANGED;
import static net.akive.api.eventprocessing.domain.ProducedEventStatus.PENDING;
import static net.akive.api.eventprocessing.domain.ProducedEventStatus.PROCESSING;
import static net.akive.api.eventprocessing.domain.fixture.ProducedEventFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

class ProducedEventJpaAdapterTest extends RepositoryTestSupport {

    @Autowired
    ProducedEventRepository producedEventRepository;

    @Test
    void 메시지_큐에_발행할_이벤트를_저장한다() {
        ProducedEvent producedEvent = ProducedEvent.create(
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                CREATED_AT
        );

        ProducedEvent savedProducedEvent = producedEventRepository.save(producedEvent);

        assertThat(savedProducedEvent.getProducedEventId()).isNotNull();
        assertThat(savedProducedEvent.getEventId()).isEqualTo(FIRST_EVENT_ID);
        assertThat(savedProducedEvent.getEventType()).isEqualTo(COACHING_TURN_DONE);
        assertThat(savedProducedEvent.getPayload()).isEqualTo(PAYLOAD);
        assertThat(savedProducedEvent.getStatus()).isEqualTo(PENDING);
        assertThat(savedProducedEvent.getRetryCount()).isZero();
        assertThat(savedProducedEvent.getNextAttemptAt()).isEqualTo(CREATED_AT);
        assertThat(savedProducedEvent.getProcessingStartedAt()).isNull();
        assertThat(savedProducedEvent.getPublishedAt()).isNull();
        assertThat(savedProducedEvent.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void 저장된_이벤트의_발행_상태를_수정한다() {
        ProducedEvent savedProducedEvent = producedEventRepository.save(ProducedEvent.create(
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                CREATED_AT
        ));
        savedProducedEvent.startProcessing(PROCESSING_STARTED_AT);

        producedEventRepository.save(savedProducedEvent);
        flushAndClear();

        ProducedEvent updatedProducedEvent = producedEventRepository
                .findByIdForUpdate(savedProducedEvent.getProducedEventId())
                .orElseThrow();

        assertThat(updatedProducedEvent.getStatus()).isEqualTo(PROCESSING);
        assertThat(updatedProducedEvent.getProcessingStartedAt()).isEqualTo(PROCESSING_STARTED_AT);
    }

    @Test
    void 같은_이벤트를_중복으로_저장할_수_없다() {
        producedEventRepository.save(ProducedEvent.create(
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                CREATED_AT
        ));

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> {
                    producedEventRepository.save(ProducedEvent.create(
                            FIRST_EVENT_ID,
                            INSIGHT_CHANGED,
                            PAYLOAD,
                            CREATED_AT
                    ));
                    entityManager.flush();
                });
    }

    @Test
    void 코칭_응답_완료_이벤트_중_다음_발행_시도_시각에_도달한_대기_이벤트만_비관적_쓰기_락으로_조회한다() {
        Instant nextAttemptAtCutoff = CREATED_AT.plusSeconds(10);
        ProducedEvent differentTypeEvent = ProducedEvent.create(
                FIRST_EVENT_ID,
                INSIGHT_CHANGED,
                PAYLOAD,
                CREATED_AT.minusSeconds(2)
        );
        ProducedEvent processingEvent = ProducedEvent.create(
                SECOND_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                CREATED_AT.minusSeconds(1)
        );
        processingEvent.startProcessing(CREATED_AT);
        ProducedEvent pendingEvent = ProducedEvent.create(
                THIRD_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                nextAttemptAtCutoff
        );
        ProducedEvent notDueEvent = ProducedEvent.create(
                FOURTH_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                nextAttemptAtCutoff.plusSeconds(1)
        );
        saveEvents(
                differentTypeEvent,
                processingEvent,
                pendingEvent,
                notDueEvent
        );
        flushAndClear();

        List<ProducedEvent> foundProducedEvents = producedEventRepository.findPendingForUpdate(
                List.of(COACHING_TURN_DONE),
                nextAttemptAtCutoff,
                10
        );

        assertThat(foundProducedEvents)
                .extracting(ProducedEvent::getEventId)
                .containsExactly(THIRD_EVENT_ID);
        assertPessimisticWriteLocks(foundProducedEvents);
    }

    @Test
    void 메시지_큐에_발행할_대기_이벤트를_오래된_순서대로_정해진_개수만큼_비관적_쓰기_락으로_조회한다() {
        Instant nextAttemptAtCutoff = CREATED_AT.plusSeconds(10);
        ProducedEvent oldestPendingEvent = ProducedEvent.create(
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                CREATED_AT
        );
        ProducedEvent secondPendingEvent = ProducedEvent.create(
                SECOND_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                CREATED_AT.plusSeconds(1)
        );
        ProducedEvent thirdPendingEvent = ProducedEvent.create(
                THIRD_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                CREATED_AT.plusSeconds(2)
        );
        saveEvents(oldestPendingEvent, secondPendingEvent, thirdPendingEvent);
        flushAndClear();

        List<ProducedEvent> foundProducedEvents = producedEventRepository.findPendingForUpdate(
                List.of(COACHING_TURN_DONE),
                nextAttemptAtCutoff,
                2
        );

        assertThat(foundProducedEvents)
                .extracting(ProducedEvent::getEventId)
                .containsExactly(FIRST_EVENT_ID, SECOND_EVENT_ID);
        assertPessimisticWriteLocks(foundProducedEvents);
    }

    @Test
    void 코칭_응답_완료_이벤트_중_발행_처리_시간이_초과된_이벤트만_비관적_쓰기_락으로_조회한다() {
        Instant processingStartedAtCutoff = CREATED_AT.plusSeconds(10);
        ProducedEvent differentTypeEvent = createProcessingProducedEvent(
                FIRST_EVENT_ID,
                INSIGHT_CHANGED,
                CREATED_AT.plusSeconds(1)
        );
        ProducedEvent pendingEvent = ProducedEvent.create(
                SECOND_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                CREATED_AT
        );
        ProducedEvent timedOutEvent = createProcessingProducedEvent(
                THIRD_EVENT_ID,
                COACHING_TURN_DONE,
                processingStartedAtCutoff
        );
        ProducedEvent notTimedOutEvent = createProcessingProducedEvent(
                FOURTH_EVENT_ID,
                COACHING_TURN_DONE,
                processingStartedAtCutoff.plusSeconds(1)
        );
        saveEvents(
                differentTypeEvent,
                pendingEvent,
                timedOutEvent,
                notTimedOutEvent
        );
        flushAndClear();

        List<ProducedEvent> foundProducedEvents = producedEventRepository.findTimedOutProcessingForUpdate(
                List.of(COACHING_TURN_DONE),
                processingStartedAtCutoff,
                10
        );

        assertThat(foundProducedEvents)
                .extracting(ProducedEvent::getEventId)
                .containsExactly(THIRD_EVENT_ID);
        assertPessimisticWriteLocks(foundProducedEvents);
    }

    @Test
    void 메시지_큐에_발행_중인_이벤트를_발행_시작_시각이_오래된_순서대로_정해진_개수만큼_비관적_쓰기_락으로_조회한다() {
        Instant processingStartedAtCutoff = CREATED_AT.plusSeconds(10);
        ProducedEvent oldestProcessingEvent = createProcessingProducedEvent(
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                CREATED_AT.plusSeconds(1)
        );
        ProducedEvent secondProcessingEvent = createProcessingProducedEvent(
                SECOND_EVENT_ID,
                COACHING_TURN_DONE,
                CREATED_AT.plusSeconds(2)
        );
        ProducedEvent thirdProcessingEvent = createProcessingProducedEvent(
                THIRD_EVENT_ID,
                COACHING_TURN_DONE,
                CREATED_AT.plusSeconds(3)
        );
        saveEvents(oldestProcessingEvent, secondProcessingEvent, thirdProcessingEvent);
        flushAndClear();

        List<ProducedEvent> foundProducedEvents = producedEventRepository.findTimedOutProcessingForUpdate(
                List.of(COACHING_TURN_DONE),
                processingStartedAtCutoff,
                2
        );

        assertThat(foundProducedEvents)
                .extracting(ProducedEvent::getEventId)
                .containsExactly(FIRST_EVENT_ID, SECOND_EVENT_ID);
        assertPessimisticWriteLocks(foundProducedEvents);
    }

    @Test
    void 메시지_큐에_발행할_이벤트_식별자로_해당_이벤트를_비관적_쓰기_락으로_조회한다() {
        ProducedEvent savedProducedEvent = producedEventRepository.save(ProducedEvent.create(
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                CREATED_AT
        ));
        flushAndClear();

        Optional<ProducedEvent> foundProducedEvent = producedEventRepository.findByIdForUpdate(
                savedProducedEvent.getProducedEventId()
        );

        assertThat(foundProducedEvent).isPresent();
        assertThat(foundProducedEvent.get().getProducedEventId())
                .isEqualTo(savedProducedEvent.getProducedEventId());
        assertThat(foundProducedEvent.get().getEventId()).isEqualTo(FIRST_EVENT_ID);
        assertPessimisticWriteLocks(List.of(foundProducedEvent.get()));
    }

    @Test
    void 메시지_큐에_발행할_이벤트_식별자로_해당_이벤트의_존재_여부를_확인한다() {
        producedEventRepository.save(ProducedEvent.create(
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                CREATED_AT
        ));
        flushAndClear();

        assertAll(
                () -> assertThat(producedEventRepository.existsByEventId(FIRST_EVENT_ID)).isTrue(),
                () -> assertThat(producedEventRepository.existsByEventId(UNKNOWN_EVENT_ID)).isFalse()
        );
    }

    private void saveEvents(ProducedEvent... producedEvents) {
        for (ProducedEvent producedEvent : producedEvents) {
            producedEventRepository.save(producedEvent);
        }
    }

    private void assertPessimisticWriteLocks(List<ProducedEvent> producedEvents) {
        for (ProducedEvent producedEvent : producedEvents) {
            ProducedEventEntity entity = entityManager.find(
                    ProducedEventEntity.class,
                    producedEvent.getProducedEventId()
            );
            assertThat(entityManager.getLockMode(entity)).isEqualTo(PESSIMISTIC_WRITE);
        }
    }
}
