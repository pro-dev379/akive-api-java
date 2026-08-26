package net.akive.api.plan.domain.value;

import net.akive.api.common.exception.business.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BadRequestError.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SessionPeriodTest {

    @Test
    void 세션_기간을_생성한다() {
        LocalDate startOn = LocalDate.of(2026, 1, 1);
        LocalDate endOn = LocalDate.of(2026, 1, 31);

        SessionPeriod sessionPeriod = new SessionPeriod(startOn, endOn);

        assertThat(sessionPeriod.startOn()).isEqualTo(startOn);
        assertThat(sessionPeriod.endOn()).isEqualTo(endOn);
    }

    @ParameterizedTest
    @MethodSource("provideNullDateSessionPeriods")
    void 시작일이나_종료일이_null이면_생성할_수_없다(LocalDate startOn, LocalDate endOn) {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new SessionPeriod(startOn, endOn))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 시작일과_종료일이_같으면_생성할_수_없다() {
        LocalDate startOn = LocalDate.of(2026, 1, 1);
        LocalDate endOn = LocalDate.of(2026, 1, 1);

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new SessionPeriod(startOn, endOn))
                .extracting(BadRequestException::getError)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 시작일이_종료일보다_이후면_생성할_수_없다() {
        LocalDate startOn = LocalDate.of(2026, 2, 1);
        LocalDate endOn = LocalDate.of(2026, 1, 31);

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new SessionPeriod(startOn, endOn))
                .extracting(BadRequestException::getError)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 최대_개월_수를_초과하면_생성할_수_없다() {
        LocalDate startOn = LocalDate.of(2026, 1, 1);
        LocalDate endOn = LocalDate.of(2027, 1, 2);

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new SessionPeriod(startOn, endOn))
                .extracting(BadRequestException::getError)
                .isEqualTo(SESSION_PERIOD_MAX_MONTHS_EXCEEDED);
    }

    @Test
    void 세션_기간을_주차_목록으로_분할한다() {
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 12)
        );

        List<SessionWeek> sessionWeeks = sessionPeriod.splitIntoWeeks();

        assertThat(sessionWeeks).containsExactly(
                SessionWeek.of(
                        1,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 4)
                ),
                SessionWeek.of(
                        2,
                        LocalDate.of(2026, 1, 5),
                        LocalDate.of(2026, 1, 11)
                ),
                SessionWeek.of(
                        3,
                        LocalDate.of(2026, 1, 12),
                        LocalDate.of(2026, 1, 12)
                )
        );
    }

    @Test
    void 주차로_세션_주차를_찾는다() {
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 12)
        );

        Optional<SessionWeek> sessionWeek = sessionPeriod.findSessionWeek(2);

        assertThat(sessionWeek).contains(
                SessionWeek.of(2,
                        LocalDate.of(2026, 1, 5),
                        LocalDate.of(2026, 1, 11)
                )
        );
    }

    @Test
    void 주차가_범위를_벗어나면_빈_값을_반환한다() {
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 12)
        );

        assertThat(sessionPeriod.findSessionWeek(0)).isEmpty();
        assertThat(sessionPeriod.findSessionWeek(4)).isEmpty();
    }

    private static Stream<Arguments> provideNullDateSessionPeriods() {
        return Stream.of(
                Arguments.arguments(null, LocalDate.of(2026, 1, 31)),
                Arguments.arguments(LocalDate.of(2026, 1, 1), null)
        );
    }
}
