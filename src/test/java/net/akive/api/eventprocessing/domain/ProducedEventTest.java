package net.akive.api.eventprocessing.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationError;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.eventprocessing.domain.EventType.COACHING_TURN_DONE;
import static net.akive.api.eventprocessing.domain.ProducedEventStatus.*;
import static net.akive.api.eventprocessing.domain.fixture.ProducedEventFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

class ProducedEventTest {

    @Test
    void 메시지_큐에_발행할_이벤트를_대기_상태로_복원한다() {
        ProducedEvent producedEvent = ProducedEvent.reconstruct(
                PRODUCED_EVENT_ID,
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                PENDING,
                RETRY_COUNT,
                NEXT_ATTEMPT_AT,
                null,
                null,
                CREATED_AT
        );

        assertThat(producedEvent.getProducedEventId()).isEqualTo(PRODUCED_EVENT_ID);
        assertThat(producedEvent.getEventId()).isEqualTo(FIRST_EVENT_ID);
        assertThat(producedEvent.getEventType()).isEqualTo(COACHING_TURN_DONE);
        assertThat(producedEvent.getPayload()).isEqualTo(PAYLOAD);
        assertThat(producedEvent.getStatus()).isEqualTo(PENDING);
        assertThat(producedEvent.getRetryCount()).isEqualTo(RETRY_COUNT);
        assertThat(producedEvent.getNextAttemptAt()).isEqualTo(NEXT_ATTEMPT_AT);
        assertThat(producedEvent.getProcessingStartedAt()).isNull();
        assertThat(producedEvent.getPublishedAt()).isNull();
        assertThat(producedEvent.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void 메시지_큐에_발행할_이벤트를_발행_중_상태로_복원한다() {
        ProducedEvent producedEvent = ProducedEvent.reconstruct(
                PRODUCED_EVENT_ID,
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                PROCESSING,
                RETRY_COUNT,
                CREATED_AT,
                PROCESSING_STARTED_AT,
                null,
                CREATED_AT
        );

        assertThat(producedEvent.getStatus()).isEqualTo(PROCESSING);
        assertThat(producedEvent.getRetryCount()).isEqualTo(RETRY_COUNT);
        assertThat(producedEvent.getNextAttemptAt()).isEqualTo(CREATED_AT);
        assertThat(producedEvent.getProcessingStartedAt()).isEqualTo(PROCESSING_STARTED_AT);
        assertThat(producedEvent.getPublishedAt()).isNull();
    }

    @Test
    void 메시지_큐에_발행할_이벤트를_발행_완료_상태로_복원한다() {
        ProducedEvent producedEvent = ProducedEvent.reconstruct(
                PRODUCED_EVENT_ID,
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                PUBLISHED,
                RETRY_COUNT,
                CREATED_AT,
                PROCESSING_STARTED_AT,
                PUBLISHED_AT,
                CREATED_AT
        );

        assertThat(producedEvent.getStatus()).isEqualTo(PUBLISHED);
        assertThat(producedEvent.getRetryCount()).isEqualTo(RETRY_COUNT);
        assertThat(producedEvent.getProcessingStartedAt()).isEqualTo(PROCESSING_STARTED_AT);
        assertThat(producedEvent.getPublishedAt()).isEqualTo(PUBLISHED_AT);
    }

    @Test
    void 메시지_큐에_발행할_이벤트를_발행_실패_상태로_복원한다() {
        ProducedEvent producedEvent = ProducedEvent.reconstruct(
                PRODUCED_EVENT_ID,
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                FAILED,
                RETRY_COUNT,
                CREATED_AT,
                PROCESSING_STARTED_AT,
                null,
                CREATED_AT
        );

        assertThat(producedEvent.getStatus()).isEqualTo(FAILED);
        assertThat(producedEvent.getRetryCount()).isEqualTo(RETRY_COUNT);
        assertThat(producedEvent.getProcessingStartedAt()).isEqualTo(PROCESSING_STARTED_AT);
        assertThat(producedEvent.getPublishedAt()).isNull();
    }

    @Test
    void 메시지_큐에_발행할_이벤트를_생성한다() {
        ProducedEvent producedEvent = ProducedEvent.create(
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                "  " + PAYLOAD + "  ",
                CREATED_AT
        );

        assertThat(producedEvent.getProducedEventId()).isNull();
        assertThat(producedEvent.getEventId()).isEqualTo(FIRST_EVENT_ID);
        assertThat(producedEvent.getEventType()).isEqualTo(COACHING_TURN_DONE);
        assertThat(producedEvent.getPayload()).isEqualTo(PAYLOAD);
        assertThat(producedEvent.getStatus()).isEqualTo(PENDING);
        assertThat(producedEvent.getRetryCount()).isZero();
        assertThat(producedEvent.getNextAttemptAt()).isEqualTo(CREATED_AT);
        assertThat(producedEvent.getProcessingStartedAt()).isNull();
        assertThat(producedEvent.getPublishedAt()).isNull();
        assertThat(producedEvent.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void 메시지_큐에_발행할_이벤트의_페이로드가_공백이면_생성할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> ProducedEvent.create(
                        FIRST_EVENT_ID,
                        COACHING_TURN_DONE,
                        "  ",
                        CREATED_AT
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 메시지_큐에_발행할_이벤트의_생성_시각보다_다음_발행_시도_시각이_빠르면_복원할_수_없다() {
        assertInvalidState(() -> reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PENDING,
                RETRY_COUNT,
                CREATED_AT.minusSeconds(1),
                null,
                null
        ));
    }

    @Test
    void 메시지_큐에_발행할_이벤트가_대기_상태인데_발행_시작_시각이_있으면_복원할_수_없다() {
        assertInvalidState(() -> reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PENDING,
                RETRY_COUNT,
                NEXT_ATTEMPT_AT,
                PROCESSING_STARTED_AT,
                null
        ));
    }

    @Test
    void 메시지_큐에_발행할_이벤트가_대기_상태인데_발행_완료_시각이_있으면_복원할_수_없다() {
        assertInvalidState(() -> reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PENDING,
                RETRY_COUNT,
                NEXT_ATTEMPT_AT,
                null,
                PUBLISHED_AT
        ));
    }

    @Test
    void 메시지_큐에_발행할_이벤트가_발행_중_상태인데_발행_시작_시각이_없으면_복원할_수_없다() {
        assertInvalidState(() -> reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PROCESSING,
                RETRY_COUNT,
                NEXT_ATTEMPT_AT,
                null,
                null
        ));
    }

    @Test
    void 메시지_큐에_발행할_이벤트가_발행_중_상태인데_발행_시작_시각이_생성_시각보다_빠르면_복원할_수_없다() {
        assertInvalidState(() -> reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PROCESSING,
                RETRY_COUNT,
                CREATED_AT,
                CREATED_AT.minusSeconds(1),
                null
        ));
    }

    @Test
    void 메시지_큐에_발행할_이벤트가_발행_중_상태인데_발행_완료_시각이_있으면_복원할_수_없다() {
        assertInvalidState(() -> reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PROCESSING,
                RETRY_COUNT,
                CREATED_AT,
                PROCESSING_STARTED_AT,
                PUBLISHED_AT
        ));
    }

    @Test
    void 메시지_큐에_발행할_이벤트가_발행_완료_상태인데_발행_시작_시각이_없으면_복원할_수_없다() {
        assertInvalidState(() -> reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PUBLISHED,
                RETRY_COUNT,
                CREATED_AT,
                null,
                PUBLISHED_AT
        ));
    }

    @Test
    void 메시지_큐에_발행할_이벤트가_발행_완료_상태인데_발행_시작_시각이_생성_시각보다_빠르면_복원할_수_없다() {
        assertInvalidState(() -> reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PUBLISHED,
                RETRY_COUNT,
                CREATED_AT,
                CREATED_AT.minusSeconds(1),
                PUBLISHED_AT
        ));
    }

    @Test
    void 메시지_큐에_발행할_이벤트가_발행_완료_상태인데_발행_완료_시각이_없으면_복원할_수_없다() {
        assertInvalidState(() -> reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PUBLISHED,
                RETRY_COUNT,
                CREATED_AT,
                PROCESSING_STARTED_AT,
                null
        ));
    }

    @Test
    void 메시지_큐에_이벤트를_발행한_시각이_발행_시작_시각보다_빠르면_복원할_수_없다() {
        assertInvalidState(() -> reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PUBLISHED,
                RETRY_COUNT,
                NEXT_ATTEMPT_AT,
                PROCESSING_STARTED_AT,
                PROCESSING_STARTED_AT.minusSeconds(1)
        ));
    }

    @Test
    void 메시지_큐에_발행할_이벤트가_발행_실패_상태인데_발행_완료_시각이_있으면_복원할_수_없다() {
        assertInvalidState(() -> reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                FAILED,
                RETRY_COUNT,
                NEXT_ATTEMPT_AT,
                PROCESSING_STARTED_AT,
                PUBLISHED_AT
        ));
    }

    @Test
    void 메시지_큐에_발행할_이벤트가_발행_실패_상태인데_발행_시작_시각이_없으면_복원할_수_없다() {
        assertInvalidState(() -> reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                FAILED,
                RETRY_COUNT,
                CREATED_AT,
                null,
                null
        ));
    }

    @Test
    void 메시지_큐에_발행할_이벤트가_발행_실패_상태인데_발행_시작_시각이_생성_시각보다_빠르면_복원할_수_없다() {
        assertInvalidState(() -> reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                FAILED,
                RETRY_COUNT,
                CREATED_AT,
                CREATED_AT.minusSeconds(1),
                null
        ));
    }

    @Test
    void 메시지_큐에_발행할_이벤트의_발행을_시작한다() {
        ProducedEvent producedEvent = createProducedEvent();

        producedEvent.startProcessing(PROCESSING_STARTED_AT);

        assertThat(producedEvent.getStatus()).isEqualTo(PROCESSING);
        assertThat(producedEvent.getProcessingStartedAt()).isEqualTo(PROCESSING_STARTED_AT);
    }

    @Test
    void 메시지_큐에_이벤트를_발행하기로_한_시각에_발행을_시작할_수_있다() {
        ProducedEvent producedEvent = reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PENDING,
                RETRY_COUNT,
                NEXT_ATTEMPT_AT,
                null,
                null
        );

        producedEvent.startProcessing(NEXT_ATTEMPT_AT);

        assertThat(producedEvent.getStatus()).isEqualTo(PROCESSING);
        assertThat(producedEvent.getProcessingStartedAt()).isEqualTo(NEXT_ATTEMPT_AT);
    }

    @Test
    void 메시지_큐에_이벤트를_발행하기로_한_시각_전에는_발행을_시작할_수_없다() {
        ProducedEvent producedEvent = createProducedEvent();
        producedEvent.startProcessing(PROCESSING_STARTED_AT);
        producedEvent.scheduleRetry(NEXT_ATTEMPT_AT);

        assertInvalidState(() -> producedEvent.startProcessing(NEXT_ATTEMPT_AT.minusSeconds(1)));
        assertThat(producedEvent.getStatus()).isEqualTo(PENDING);
        assertThat(producedEvent.getRetryCount()).isEqualTo(1);
        assertThat(producedEvent.getNextAttemptAt()).isEqualTo(NEXT_ATTEMPT_AT);
        assertThat(producedEvent.getProcessingStartedAt()).isNull();
    }

    @Test
    void 메시지_큐에_발행할_이벤트가_대기하고_있지_않으면_발행을_시작할_수_없다() {
        ProducedEvent producedEvent = createProducedEvent();
        producedEvent.startProcessing(PROCESSING_STARTED_AT);

        assertInvalidStatusTransition(() -> producedEvent.startProcessing(PROCESSING_STARTED_AT));
    }

    @Test
    void 메시지_큐에_발행_중인_이벤트의_발행을_완료한다() {
        ProducedEvent producedEvent = createProducedEvent();
        producedEvent.startProcessing(PROCESSING_STARTED_AT);

        producedEvent.markPublished(PUBLISHED_AT);

        assertThat(producedEvent.getStatus()).isEqualTo(PUBLISHED);
        assertThat(producedEvent.getPublishedAt()).isEqualTo(PUBLISHED_AT);
    }

    @Test
    void 이벤트_발행을_시작한_시각에_발행을_완료할_수_있다() {
        ProducedEvent producedEvent = createProducedEvent();
        producedEvent.startProcessing(PROCESSING_STARTED_AT);

        producedEvent.markPublished(PROCESSING_STARTED_AT);

        assertThat(producedEvent.getStatus()).isEqualTo(PUBLISHED);
        assertThat(producedEvent.getPublishedAt()).isEqualTo(PROCESSING_STARTED_AT);
    }

    @Test
    void 이벤트_발행을_시작한_시각보다_빠르게_발행을_완료할_수_없다() {
        ProducedEvent producedEvent = createProducedEvent();
        producedEvent.startProcessing(PROCESSING_STARTED_AT);

        assertInvalidState(() -> producedEvent.markPublished(CREATED_AT));
        assertThat(producedEvent.getStatus()).isEqualTo(PROCESSING);
        assertThat(producedEvent.getRetryCount()).isZero();
        assertThat(producedEvent.getProcessingStartedAt()).isEqualTo(PROCESSING_STARTED_AT);
        assertThat(producedEvent.getPublishedAt()).isNull();
    }

    @Test
    void 메시지_큐에_발행_중인_이벤트의_재발행을_예약한다() {
        ProducedEvent producedEvent = reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PROCESSING,
                RETRY_COUNT,
                CREATED_AT,
                PROCESSING_STARTED_AT,
                null
        );

        producedEvent.scheduleRetry(NEXT_ATTEMPT_AT);

        assertThat(producedEvent.getStatus()).isEqualTo(PENDING);
        assertThat(producedEvent.getRetryCount()).isEqualTo(RETRY_COUNT + 1);
        assertThat(producedEvent.getNextAttemptAt()).isEqualTo(NEXT_ATTEMPT_AT);
        assertThat(producedEvent.getProcessingStartedAt()).isNull();
    }

    @Test
    void 이벤트_발행을_시작한_시각에_재발행을_예약할_수_있다() {
        ProducedEvent producedEvent = createProducedEvent();
        producedEvent.startProcessing(PROCESSING_STARTED_AT);

        producedEvent.scheduleRetry(PROCESSING_STARTED_AT);

        assertThat(producedEvent.getStatus()).isEqualTo(PENDING);
        assertThat(producedEvent.getRetryCount()).isEqualTo(1);
        assertThat(producedEvent.getNextAttemptAt()).isEqualTo(PROCESSING_STARTED_AT);
        assertThat(producedEvent.getProcessingStartedAt()).isNull();
    }

    @Test
    void 이벤트_발행을_시작한_시각보다_빠르게_재발행을_예약할_수_없다() {
        ProducedEvent producedEvent = createProducedEvent();
        producedEvent.startProcessing(PROCESSING_STARTED_AT);

        assertInvalidState(() -> producedEvent.scheduleRetry(CREATED_AT));
        assertThat(producedEvent.getStatus()).isEqualTo(PROCESSING);
        assertThat(producedEvent.getRetryCount()).isZero();
        assertThat(producedEvent.getNextAttemptAt()).isEqualTo(CREATED_AT);
        assertThat(producedEvent.getProcessingStartedAt()).isEqualTo(PROCESSING_STARTED_AT);
    }

    @Test
    void 메시지_큐에_발행_중인_이벤트를_재발행하도록_복구한다() {
        ProducedEvent producedEvent = reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PROCESSING,
                RETRY_COUNT,
                CREATED_AT,
                PROCESSING_STARTED_AT,
                null
        );

        producedEvent.recoverTimedOutProcessing(NEXT_ATTEMPT_AT);

        assertThat(producedEvent.getStatus()).isEqualTo(PENDING);
        assertThat(producedEvent.getRetryCount()).isEqualTo(RETRY_COUNT + 1);
        assertThat(producedEvent.getNextAttemptAt()).isEqualTo(NEXT_ATTEMPT_AT);
        assertThat(producedEvent.getProcessingStartedAt()).isNull();
    }

    @Test
    void 재시도_후_메시지_큐에_이벤트를_발행해도_누적_재시도_횟수를_유지한다() {
        ProducedEvent producedEvent = reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PROCESSING,
                RETRY_COUNT,
                CREATED_AT,
                PROCESSING_STARTED_AT,
                null
        );
        producedEvent.scheduleRetry(NEXT_ATTEMPT_AT);
        producedEvent.startProcessing(NEXT_ATTEMPT_AT);

        producedEvent.markPublished(NEXT_ATTEMPT_AT);

        assertThat(producedEvent.getStatus()).isEqualTo(PUBLISHED);
        assertThat(producedEvent.getRetryCount()).isEqualTo(RETRY_COUNT + 1);
    }

    @Test
    void 메시지_큐에_발행_중인_이벤트를_실패_상태로_변경한다() {
        ProducedEvent producedEvent = reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PROCESSING,
                RETRY_COUNT,
                CREATED_AT,
                PROCESSING_STARTED_AT,
                null
        );

        producedEvent.markFailed();

        assertThat(producedEvent.getStatus()).isEqualTo(FAILED);
        assertThat(producedEvent.getRetryCount()).isEqualTo(RETRY_COUNT + 1);
        assertThat(producedEvent.getProcessingStartedAt()).isEqualTo(PROCESSING_STARTED_AT);
    }

    @Test
    void 이벤트가_메시지_큐에_발행_중인_상태가_아니면_발행_상태를_변경할_수_없다() {
        ProducedEvent producedEvent = createProducedEvent();

        assertAll(
                () -> assertInvalidStatusTransition(() -> producedEvent.markPublished(PUBLISHED_AT)),
                () -> assertInvalidStatusTransition(() -> producedEvent.scheduleRetry(NEXT_ATTEMPT_AT)),
                () -> assertInvalidStatusTransition(
                        () -> producedEvent.recoverTimedOutProcessing(NEXT_ATTEMPT_AT)
                ),
                () -> assertInvalidStatusTransition(producedEvent::markFailed)
        );
    }

    @ParameterizedTest
    @EnumSource(value = ProducedEventStatus.class, names = {"PUBLISHED", "FAILED"})
    void 메시지_큐에_이벤트를_발행하는_과정이_끝나면_상태를_변경할_수_없다(ProducedEventStatus status) {
        ProducedEvent producedEvent = reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                status,
                RETRY_COUNT,
                CREATED_AT,
                PROCESSING_STARTED_AT,
                status == PUBLISHED ? PUBLISHED_AT : null
        );

        assertAll(
                () -> assertInvalidStatusTransition(() -> producedEvent.startProcessing(NEXT_ATTEMPT_AT)),
                () -> assertInvalidStatusTransition(() -> producedEvent.markPublished(PUBLISHED_AT)),
                () -> assertInvalidStatusTransition(() -> producedEvent.scheduleRetry(NEXT_ATTEMPT_AT)),
                () -> assertInvalidStatusTransition(
                        () -> producedEvent.recoverTimedOutProcessing(NEXT_ATTEMPT_AT)
                ),
                () -> assertInvalidStatusTransition(producedEvent::markFailed)
        );

        assertThat(producedEvent.getStatus()).isEqualTo(status);
        assertThat(producedEvent.getRetryCount()).isEqualTo(RETRY_COUNT);
        assertThat(producedEvent.getProcessingStartedAt()).isEqualTo(PROCESSING_STARTED_AT);
        assertThat(producedEvent.getPublishedAt()).isEqualTo(status == PUBLISHED ? PUBLISHED_AT : null);
    }

    @Test
    void 이벤트_발행_상태_변경_시각이_없으면_상태를_변경할_수_없다() {
        ProducedEvent pendingEvent = createProducedEvent();
        ProducedEvent eventToPublish = createProducedEvent();
        ProducedEvent eventToRetry = createProducedEvent();
        ProducedEvent timedOutEvent = createProducedEvent();
        eventToPublish.startProcessing(PROCESSING_STARTED_AT);
        eventToRetry.startProcessing(PROCESSING_STARTED_AT);
        timedOutEvent.startProcessing(PROCESSING_STARTED_AT);

        assertAll(
                () -> assertValidationError(
                        () -> pendingEvent.startProcessing(null),
                        REQUIRED_VALUE_MISSING
                ),
                () -> assertValidationError(
                        () -> eventToPublish.markPublished(null),
                        REQUIRED_VALUE_MISSING
                ),
                () -> assertValidationError(
                        () -> eventToRetry.scheduleRetry(null),
                        REQUIRED_VALUE_MISSING
                ),
                () -> assertValidationError(
                        () -> timedOutEvent.recoverTimedOutProcessing(null),
                        REQUIRED_VALUE_MISSING
                )
        );

        assertThat(pendingEvent.getStatus()).isEqualTo(PENDING);
        assertThat(eventToPublish.getStatus()).isEqualTo(PROCESSING);
        assertThat(eventToRetry.getStatus()).isEqualTo(PROCESSING);
        assertThat(timedOutEvent.getStatus()).isEqualTo(PROCESSING);
    }

    @Test
    void 메시지_큐에_발행할_이벤트는_식별자가_같으면_같게_취급한다() {
        ProducedEvent producedEvent = reconstructProducedEvent();
        ProducedEvent sameProducedEvent = reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PENDING,
                0,
                CREATED_AT,
                null,
                null
        );

        assertThat(producedEvent).isEqualTo(sameProducedEvent);
        assertThat(producedEvent.hashCode()).isEqualTo(sameProducedEvent.hashCode());
    }

    @Test
    void 메시지_큐에_발행할_이벤트는_식별자가_다르면_다르게_취급한다() {
        ProducedEvent producedEvent = reconstructProducedEvent();
        ProducedEvent anotherProducedEvent = reconstructProducedEvent(
                ANOTHER_PRODUCED_EVENT_ID,
                PENDING,
                RETRY_COUNT,
                NEXT_ATTEMPT_AT,
                null,
                null
        );

        assertThat(producedEvent).isNotEqualTo(anotherProducedEvent);
    }

    @Test
    void 메시지_큐에_발행할_이벤트는_식별자가_없으면_나머지_값이_같더라도_다르게_취급한다() {
        ProducedEvent producedEvent = createProducedEvent();
        ProducedEvent anotherProducedEvent = createProducedEvent();

        assertThat(producedEvent).isNotEqualTo(anotherProducedEvent);
    }

    private static void assertInvalidState(ThrowableAssert.ThrowingCallable callable) {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(callable)
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_PRODUCED_EVENT_STATE);
    }

    private static void assertInvalidStatusTransition(ThrowableAssert.ThrowingCallable callable) {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(callable)
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_PRODUCED_EVENT_STATUS_TRANSITION);
    }

    private static void assertValidationError(
            ThrowableAssert.ThrowingCallable callable,
            BusinessRuleViolationError error
    ) {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(callable)
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(error);
    }
}
