package net.akive.api.plan.application.service;

import net.akive.api.plan.application.port.out.ValidateTotalQuantityPort;
import net.akive.api.plan.exception.PlanException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.plan.exception.PlanErrorCode.SUBJECT_NAME_ALREADY_EXISTS;
import static net.akive.api.plan.exception.PlanErrorCode.TOTAL_QUANTITY_LIMIT_EXCEEDED;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TotalQuantityValidatorTest {

    private static final Long SESSIONAL_PLAN_ID = 1L;
    private static final String SUBJECT_NAME = "수학";

    @Mock
    ValidateTotalQuantityPort validateTotalQuantityPort;

    @InjectMocks
    TotalQuantityValidator totalQuantityValidator;

    @Test
    void 과목명이_중복되지_않고_세션_총량_개수가_50개_미만이면_생성할_수_있다() {
        // given
        given(validateTotalQuantityPort.existsBySessionalPlanIdAndSubjectName(SESSIONAL_PLAN_ID, SUBJECT_NAME))
                .willReturn(false);
        given(validateTotalQuantityPort.countBySessionalPlanId(SESSIONAL_PLAN_ID))
                .willReturn(49);

        // when & then
        assertThatCode(() -> totalQuantityValidator.validateCreatable(SESSIONAL_PLAN_ID, SUBJECT_NAME))
                .doesNotThrowAnyException();

        then(validateTotalQuantityPort).should()
                .existsBySessionalPlanIdAndSubjectName(SESSIONAL_PLAN_ID, SUBJECT_NAME);
        then(validateTotalQuantityPort).should().countBySessionalPlanId(SESSIONAL_PLAN_ID);
    }

    @Test
    void 과목명이_중복되면_세션_총량을_생성할_수_없다() {
        // given
        given(validateTotalQuantityPort.existsBySessionalPlanIdAndSubjectName(SESSIONAL_PLAN_ID, SUBJECT_NAME))
                .willReturn(true);

        // when & then
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> totalQuantityValidator.validateCreatable(SESSIONAL_PLAN_ID, SUBJECT_NAME))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(SUBJECT_NAME_ALREADY_EXISTS);

        then(validateTotalQuantityPort).should()
                .existsBySessionalPlanIdAndSubjectName(SESSIONAL_PLAN_ID, SUBJECT_NAME);
        then(validateTotalQuantityPort).should(never()).countBySessionalPlanId(SESSIONAL_PLAN_ID);
    }

    @Test
    void 세션_총량_개수가_50개_이상이면_추가로_생성할_수_없다() {
        // given
        given(validateTotalQuantityPort.existsBySessionalPlanIdAndSubjectName(SESSIONAL_PLAN_ID, SUBJECT_NAME))
                .willReturn(false);
        given(validateTotalQuantityPort.countBySessionalPlanId(SESSIONAL_PLAN_ID))
                .willReturn(50);

        // when & then
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> totalQuantityValidator.validateCreatable(SESSIONAL_PLAN_ID, SUBJECT_NAME))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(TOTAL_QUANTITY_LIMIT_EXCEEDED);

        then(validateTotalQuantityPort).should()
                .existsBySessionalPlanIdAndSubjectName(SESSIONAL_PLAN_ID, SUBJECT_NAME);
        then(validateTotalQuantityPort).should().countBySessionalPlanId(SESSIONAL_PLAN_ID);
    }
}