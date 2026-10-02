package net.akive.api.common.time;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.akive.api.common.time.ServiceDayPolicy.toInstantAtServiceZone;
import static net.akive.api.common.time.ServiceDayPolicy.toKoreanDate;
import static net.akive.api.common.time.ServiceDayPolicy.toServiceDate;
import static net.akive.api.common.time.ServiceDayPolicy.toServiceDayStart;
import static org.assertj.core.api.Assertions.assertThat;

class ServiceDayPolicyTest {

    @Test
    void 절대_시각을_한국_날짜로_변환한다() {
        Instant octoberFirstAtFourPm = Instant.parse("2026-10-01T16:00:00Z");

        LocalDate koreanDate = toKoreanDate(octoberFirstAtFourPm);

        assertThat(koreanDate).isEqualTo(LocalDate.of(2026, 10, 2));
    }

    @Test
    void 한국_시간_기준으로_자정_직전까지는_이전_날짜다() {
        Instant octoberFirstAtElevenFiftyNine = Instant.parse("2026-10-01T14:59:59.999999999Z");

        LocalDate koreanDate = toKoreanDate(octoberFirstAtElevenFiftyNine);

        assertThat(koreanDate).isEqualTo(LocalDate.of(2026, 10, 1));
    }

    @Test
    void 한국_시간_기준으로_자정부터_새로운_날짜다() {
        Instant octoberSecondAtMidnight = Instant.parse("2026-10-01T15:00:00Z");

        LocalDate koreanDate = toKoreanDate(octoberSecondAtMidnight);

        assertThat(koreanDate).isEqualTo(LocalDate.of(2026, 10, 2));
    }

    @Test
    void 서비스_시간대의_날짜와_시간을_절대_시각으로_변환한다() {
        LocalDateTime octoberSecondAtOne = LocalDateTime.of(2026, 10, 2, 1, 0);

        Instant instant = toInstantAtServiceZone(octoberSecondAtOne);

        assertThat(instant).isEqualTo(Instant.parse("2026-10-01T16:00:00Z"));
    }

    @Test
    void 한국_시간_기준으로_오전_5시_직전은_이전_서비스일에_속한다() {
        Instant octoberSecondImmediatelyBeforeFive =
                Instant.parse("2026-10-01T19:59:59.999999999Z");

        LocalDate serviceDate = toServiceDate(octoberSecondImmediatelyBeforeFive);

        assertThat(serviceDate).isEqualTo(LocalDate.of(2026, 10, 1));
    }

    @Test
    void 한국_시간_기준으로_오전_5시부터_새로운_서비스일에_속한다() {
        Instant octoberSecondAtFive = Instant.parse("2026-10-01T20:00:00Z");

        LocalDate serviceDate = toServiceDate(octoberSecondAtFive);

        assertThat(serviceDate).isEqualTo(LocalDate.of(2026, 10, 2));
    }

    @Test
    void 서비스일은_한국_시간_기준으로_오전_5시에_시작한다() {
        Instant startedAt = toServiceDayStart(LocalDate.of(2026, 10, 2));

        assertThat(startedAt).isEqualTo(Instant.parse("2026-10-01T20:00:00Z"));
    }
}
