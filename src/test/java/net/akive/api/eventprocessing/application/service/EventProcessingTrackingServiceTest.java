package net.akive.api.eventprocessing.application.service;

import net.akive.api.eventprocessing.application.required.ProcessedEventRepository;
import net.akive.api.eventprocessing.domain.ProcessedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.eventprocessing.domain.EventConsumer.LEARNER_INSIGHT;
import static net.akive.api.eventprocessing.domain.EventType.COACHING_TURN_DONE;
import static net.akive.api.eventprocessing.domain.fixture.ProcessedEventFixture.EVENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EventProcessingTrackingServiceTest {

    @Mock
    ProcessedEventRepository processedEventRepository;

    @InjectMocks
    EventProcessingTrackingService eventProcessingTrackingService;

    @Test
    void 소비자의_이벤트_처리_완료를_기록한다() {
        eventProcessingTrackingService.markProcessed(EVENT_ID, COACHING_TURN_DONE, LEARNER_INSIGHT);

        ArgumentCaptor<ProcessedEvent> processedEventCaptor = ArgumentCaptor.forClass(ProcessedEvent.class);
        then(processedEventRepository).should().save(processedEventCaptor.capture());
        ProcessedEvent processedEvent = processedEventCaptor.getValue();
        assertThat(processedEvent.getEventId()).isEqualTo(EVENT_ID);
        assertThat(processedEvent.getEventType()).isEqualTo(COACHING_TURN_DONE);
        assertThat(processedEvent.getConsumer()).isEqualTo(LEARNER_INSIGHT);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void 소비자가_이벤트를_처리했는지_확인한다(boolean expectedProcessed) {
        given(processedEventRepository.existsByEventIdAndConsumer(EVENT_ID, LEARNER_INSIGHT))
                .willReturn(expectedProcessed);

        boolean processed = eventProcessingTrackingService.isProcessed(EVENT_ID, LEARNER_INSIGHT);

        assertThat(processed).isEqualTo(expectedProcessed);
    }
}
