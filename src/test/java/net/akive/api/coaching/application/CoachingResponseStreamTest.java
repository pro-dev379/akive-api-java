package net.akive.api.coaching.application;

import net.akive.api.coaching.application.stream.CoachingResponseStream;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class CoachingResponseStreamTest {

    @Test
    void 구독에_실패해도_종료_작업을_실행한다() {
        IllegalStateException subscribeException = new IllegalStateException("Failed to subscribe.");
        CoachingResponseStream upstream = new CoachingResponseStream() {
            @Override
            public void subscribe(Listener downstreamListener) {
                throw subscribeException;
            }

            @Override
            public void cancel() {
            }
        };
        AtomicInteger terminationCount = new AtomicInteger();
        CoachingResponseStream coachingResponseStream = upstream.onTermination(terminationCount::incrementAndGet);

        assertThatThrownBy(() -> coachingResponseStream.subscribe(mock(CoachingResponseStream.Listener.class)))
                .isSameAs(subscribeException);
        coachingResponseStream.cancel();

        assertThat(terminationCount).hasValue(1);
    }
}
