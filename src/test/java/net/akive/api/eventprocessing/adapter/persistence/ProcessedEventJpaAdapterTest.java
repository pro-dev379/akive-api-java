package net.akive.api.eventprocessing.adapter.persistence;

import net.akive.api.eventprocessing.application.required.ProcessedEventRepository;
import net.akive.api.eventprocessing.domain.ProcessedEvent;
import net.akive.api.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static net.akive.api.eventprocessing.domain.EventConsumer.LEARNER_INSIGHT;
import static net.akive.api.eventprocessing.domain.EventType.COACHING_TURN_DONE;
import static net.akive.api.eventprocessing.domain.fixture.ProcessedEventFixture.EVENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ProcessedEventJpaAdapterTest extends RepositoryTestSupport {

    @Autowired
    ProcessedEventRepository processedEventRepository;

    @Test
    void 처리된_이벤트를_저장한다() {
        ProcessedEvent processedEvent = ProcessedEvent.create(
                EVENT_ID,
                COACHING_TURN_DONE,
                LEARNER_INSIGHT
        );

        ProcessedEvent savedProcessedEvent = processedEventRepository.save(processedEvent);

        assertThat(savedProcessedEvent.getProcessedEventId()).isNotNull();
        assertThat(savedProcessedEvent.getEventId()).isEqualTo(EVENT_ID);
        assertThat(savedProcessedEvent.getEventType()).isEqualTo(COACHING_TURN_DONE);
        assertThat(savedProcessedEvent.getConsumer()).isEqualTo(LEARNER_INSIGHT);
        assertThat(savedProcessedEvent.getCreatedAt()).isNotNull();
    }

    @Test
    void 같은_이벤트를_같은_소비자가_중복으로_저장할_수_없다() {
        processedEventRepository.save(ProcessedEvent.create(
                EVENT_ID,
                COACHING_TURN_DONE,
                LEARNER_INSIGHT
        ));

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> {
                    processedEventRepository.save(ProcessedEvent.create(
                            EVENT_ID,
                            COACHING_TURN_DONE,
                            LEARNER_INSIGHT
                    ));
                    entityManager.flush();
                });
    }

    @Test
    void 이벤트_식별자와_소비자_타입으로_처리된_이벤트_존재_여부를_확인한다() {
        processedEventRepository.save(ProcessedEvent.create(
                EVENT_ID,
                COACHING_TURN_DONE,
                LEARNER_INSIGHT
        ));
        flushAndClear();

        boolean exists = processedEventRepository.existsByEventIdAndConsumer(EVENT_ID, LEARNER_INSIGHT);

        assertThat(exists).isTrue();
    }
}
