package net.akive.api.plan.domain;

import net.akive.api.common.exception.business.BadRequestException;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static java.time.DayOfWeek.TUESDAY;
import static net.akive.api.common.exception.business.BadRequestError.REQUIRED_VALUE_MISSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PlanDateTest {

    @Test
    void 계획_날짜를_생성한다() {
        // given
        LocalDate date = LocalDate.of(2026, 7, 7);

        // when
        PlanDate planDate = new PlanDate(date, TUESDAY);

        // then
        assertThat(planDate.date()).isEqualTo(date);
        assertThat(planDate.dayOfWeek()).isEqualTo(TUESDAY);
    }

    @Test
    void 날짜로_계획_날짜를_생성한다() {
        // when
        PlanDate planDate = PlanDate.from(LocalDate.of(2026, 7, 7));

        // then
        assertThat(planDate).isEqualTo(new PlanDate(LocalDate.of(2026, 7, 7), TUESDAY));
    }

    @Test
    void 날짜가_null이면_계획_날짜_생성에_실패한다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new PlanDate(null, TUESDAY))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 요일이_null이면_계획_날짜_생성에_실패한다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new PlanDate(LocalDate.of(2026, 7, 7), null))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }
}
