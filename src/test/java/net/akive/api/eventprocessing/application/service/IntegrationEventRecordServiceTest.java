package net.akive.api.eventprocessing.application.service;

import net.akive.api.eventprocessing.application.event.IntegrationEvent;
import net.akive.api.eventprocessing.application.required.IntegrationEventSerializer;
import net.akive.api.eventprocessing.application.required.ProducedEventRepository;
import net.akive.api.eventprocessing.domain.ProducedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.eventprocessing.domain.EventType.COACHING_TURN_DONE;
import static net.akive.api.eventprocessing.domain.fixture.ProducedEventFixture.CREATED_AT;
import static net.akive.api.eventprocessing.domain.fixture.ProducedEventFixture.FIRST_EVENT_ID;
import static net.akive.api.eventprocessing.domain.fixture.ProducedEventFixture.PAYLOAD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class IntegrationEventRecordServiceTest {

    @Mock
    IntegrationEventSerializer integrationEventSerializer;

    @Mock
    ProducedEventRepository producedEventRepository;

    @Mock
    IntegrationEvent integrationEvent;

    @InjectMocks
    IntegrationEventRecordService integrationEventRecordService;

    @Test
    void 통합_이벤트를_직렬화해서_메시지_큐에_발행할_이벤트로_저장한다() {
        given(integrationEvent.eventId()).willReturn(FIRST_EVENT_ID);
        given(integrationEvent.eventType()).willReturn(COACHING_TURN_DONE);
        given(integrationEvent.createdAt()).willReturn(CREATED_AT);
        given(producedEventRepository.existsByEventId(FIRST_EVENT_ID)).willReturn(false);
        given(integrationEventSerializer.serialize(integrationEvent)).willReturn(PAYLOAD);

        integrationEventRecordService.record(integrationEvent);

        ArgumentCaptor<ProducedEvent> producedEventCaptor = ArgumentCaptor.forClass(ProducedEvent.class);
        then(producedEventRepository).should().save(producedEventCaptor.capture());
        ProducedEvent producedEvent = producedEventCaptor.getValue();
        assertThat(producedEvent.getEventId()).isEqualTo(FIRST_EVENT_ID);
        assertThat(producedEvent.getEventType()).isEqualTo(COACHING_TURN_DONE);
        assertThat(producedEvent.getPayload()).isEqualTo(PAYLOAD);
        assertThat(producedEvent.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void 같은_통합_이벤트가_이미_저장되어_있으면_중복으로_저장하지_않는다() {
        given(integrationEvent.eventId()).willReturn(FIRST_EVENT_ID);
        given(producedEventRepository.existsByEventId(FIRST_EVENT_ID)).willReturn(true);

        integrationEventRecordService.record(integrationEvent);

        then(integrationEventSerializer).should(never()).serialize(any(IntegrationEvent.class));
        then(producedEventRepository).should(never()).save(any(ProducedEvent.class));
    }
}
