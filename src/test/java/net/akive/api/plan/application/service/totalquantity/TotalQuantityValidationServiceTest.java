package net.akive.api.plan.application.service.totalquantity;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.TOTAL_QUANTITY_LIMIT_EXCEEDED;
import static net.akive.api.common.exception.business.ResourceConflictError.SUBJECT_NAME_ALREADY_EXISTS;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.SESSIONAL_PLAN_ID;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.FIRST_SUBJECT_NAME;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.FIRST_TOTAL_QUANTITY_ID;
import static net.akive.api.plan.domain.policy.PlanPolicy.TOTAL_QUANTITY_MAX_COUNT;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TotalQuantityValidationServiceTest {

    @Mock
    TotalQuantityRepository totalQuantityRepository;

    @InjectMocks
    TotalQuantityValidationService totalQuantityValidationService;

    @Test
    void 세션_총량을_생성할_수_있는지_검증한다() {
        given(totalQuantityRepository.existsBySessionalPlanIdAndSubjectName(SESSIONAL_PLAN_ID, FIRST_SUBJECT_NAME))
                .willReturn(false);
        given(totalQuantityRepository.countBySessionalPlanId(SESSIONAL_PLAN_ID))
                .willReturn(TOTAL_QUANTITY_MAX_COUNT - 1);

        totalQuantityValidationService.validateCreatable(SESSIONAL_PLAN_ID, FIRST_SUBJECT_NAME);

        then(totalQuantityRepository).should()
                .existsBySessionalPlanIdAndSubjectName(SESSIONAL_PLAN_ID, FIRST_SUBJECT_NAME);
        then(totalQuantityRepository).should().countBySessionalPlanId(SESSIONAL_PLAN_ID);
    }

    @Test
    void 같은_과목명의_세션_총량이_있으면_생성할_수_없다() {
        given(totalQuantityRepository.existsBySessionalPlanIdAndSubjectName(SESSIONAL_PLAN_ID, FIRST_SUBJECT_NAME))
                .willReturn(true);

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> totalQuantityValidationService.validateCreatable(
                        SESSIONAL_PLAN_ID,
                        FIRST_SUBJECT_NAME
                ))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(SUBJECT_NAME_ALREADY_EXISTS);

        then(totalQuantityRepository).should()
                .existsBySessionalPlanIdAndSubjectName(SESSIONAL_PLAN_ID, FIRST_SUBJECT_NAME);
        then(totalQuantityRepository).should(never()).countBySessionalPlanId(SESSIONAL_PLAN_ID);
    }

    @Test
    void 세션_총량이_최대치에_도달하면_생성할_수_없다() {
        given(totalQuantityRepository.existsBySessionalPlanIdAndSubjectName(SESSIONAL_PLAN_ID, FIRST_SUBJECT_NAME))
                .willReturn(false);
        given(totalQuantityRepository.countBySessionalPlanId(SESSIONAL_PLAN_ID))
                .willReturn(TOTAL_QUANTITY_MAX_COUNT);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> totalQuantityValidationService.validateCreatable(
                        SESSIONAL_PLAN_ID,
                        FIRST_SUBJECT_NAME
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(TOTAL_QUANTITY_LIMIT_EXCEEDED);

        then(totalQuantityRepository).should()
                .existsBySessionalPlanIdAndSubjectName(SESSIONAL_PLAN_ID, FIRST_SUBJECT_NAME);
        then(totalQuantityRepository).should().countBySessionalPlanId(SESSIONAL_PLAN_ID);
    }

    @Test
    void 세션_총량을_수정할_수_있는지_검증한다() {
        given(totalQuantityRepository.existsBySessionalPlanIdAndSubjectNameAndTotalQuantityIdNot(
                SESSIONAL_PLAN_ID,
                FIRST_SUBJECT_NAME,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(false);

        totalQuantityValidationService.validateUpdatable(FIRST_TOTAL_QUANTITY_ID, SESSIONAL_PLAN_ID, FIRST_SUBJECT_NAME);

        then(totalQuantityRepository).should().existsBySessionalPlanIdAndSubjectNameAndTotalQuantityIdNot(
                SESSIONAL_PLAN_ID,
                FIRST_SUBJECT_NAME,
                FIRST_TOTAL_QUANTITY_ID
        );
    }

    @Test
    void 같은_과목명의_다른_세션_총량이_있으면_수정할_수_없다() {
        given(totalQuantityRepository.existsBySessionalPlanIdAndSubjectNameAndTotalQuantityIdNot(
                SESSIONAL_PLAN_ID,
                FIRST_SUBJECT_NAME,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(true);

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> totalQuantityValidationService.validateUpdatable(
                        FIRST_TOTAL_QUANTITY_ID,
                        SESSIONAL_PLAN_ID,
                        FIRST_SUBJECT_NAME
                ))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(SUBJECT_NAME_ALREADY_EXISTS);

        then(totalQuantityRepository).should().existsBySessionalPlanIdAndSubjectNameAndTotalQuantityIdNot(
                SESSIONAL_PLAN_ID,
                FIRST_SUBJECT_NAME,
                FIRST_TOTAL_QUANTITY_ID
        );
    }
}
