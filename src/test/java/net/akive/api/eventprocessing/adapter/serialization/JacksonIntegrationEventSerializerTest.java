package net.akive.api.eventprocessing.adapter.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.eventprocessing.application.event.IntegrationEvent;
import net.akive.api.eventprocessing.application.exception.IntegrationEventSerializationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.eventprocessing.domain.fixture.ProducedEventFixture.FIRST_EVENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JacksonIntegrationEventSerializerTest {

    private static final String SERIALIZED_EVENT = """
            {"eventId":"0199a8e2-7c31-7abc-8123-456789abcdef","eventType":"COACHING_TURN_DONE"}
            """;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    IntegrationEvent integrationEvent;

    @InjectMocks
    JacksonIntegrationEventSerializer integrationEventSerializer;

    @Test
    void 통합_이벤트를_JSON으로_직렬화한다() throws JsonProcessingException {
        given(objectMapper.writeValueAsString(integrationEvent)).willReturn(SERIALIZED_EVENT);

        String serializedEvent = integrationEventSerializer.serialize(integrationEvent);

        assertThat(serializedEvent).isEqualTo(SERIALIZED_EVENT);
        then(objectMapper).should().writeValueAsString(integrationEvent);
    }

    @Test
    void 통합_이벤트를_JSON으로_직렬화할_수_없으면_직렬화_예외를_던진다() throws JsonProcessingException {
        JsonProcessingException serializationException = mock(JsonProcessingException.class);
        given(integrationEvent.eventId()).willReturn(FIRST_EVENT_ID);
        given(objectMapper.writeValueAsString(integrationEvent)).willThrow(serializationException);

        assertThatExceptionOfType(IntegrationEventSerializationException.class)
                .isThrownBy(() -> integrationEventSerializer.serialize(integrationEvent))
                .withMessage("Failed to serialize integration event: " + FIRST_EVENT_ID)
                .withCause(serializationException);
    }
}
