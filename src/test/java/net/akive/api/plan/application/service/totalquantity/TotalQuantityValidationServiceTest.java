package net.akive.api.plan.application.service.totalquantity;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.common.exception.business.BadRequestError.TOTAL_QUANTITY_LIMIT_EXCEEDED;
import static net.akive.api.common.exception.business.ConflictError.SUBJECT_NAME_ALREADY_EXISTS;
import static net.akive.api.plan.domain.PlanFixture.SUBJECT_NAME;
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
        Long sessionalPlanId = 1L;
        given(totalQuantityRepository.existsBySessionalPlanIdAndSubjectName(sessionalPlanId, SUBJECT_NAME))
                .willReturn(false);
        given(totalQuantityRepository.countBySessionalPlanId(sessionalPlanId))
                .willReturn(TOTAL_QUANTITY_MAX_COUNT - 1);

        totalQuantityValidationService.validateCreatable(sessionalPlanId, SUBJECT_NAME);

        then(totalQuantityRepository).should()
                .existsBySessionalPlanIdAndSubjectName(sessionalPlanId, SUBJECT_NAME);
        then(totalQuantityRepository).should().countBySessionalPlanId(sessionalPlanId);
    }

    @Test
    void 같은_과목명의_세션_총량이_있으면_생성할_수_없다() {
        Long sessionalPlanId = 1L;
        given(totalQuantityRepository.existsBySessionalPlanIdAndSubjectName(sessionalPlanId, SUBJECT_NAME))
                .willReturn(true);

        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> totalQuantityValidationService.validateCreatable(
                        sessionalPlanId,
                        SUBJECT_NAME
                ))
                .extracting(ConflictException::getError)
                .isEqualTo(SUBJECT_NAME_ALREADY_EXISTS);

        then(totalQuantityRepository).should()
                .existsBySessionalPlanIdAndSubjectName(sessionalPlanId, SUBJECT_NAME);
        then(totalQuantityRepository).should(never()).countBySessionalPlanId(sessionalPlanId);
    }

    @Test
    void 세션_총량이_최대치에_도달하면_생성할_수_없다() {
        Long sessionalPlanId = 1L;
        given(totalQuantityRepository.existsBySessionalPlanIdAndSubjectName(sessionalPlanId, SUBJECT_NAME))
                .willReturn(false);
        given(totalQuantityRepository.countBySessionalPlanId(sessionalPlanId))
                .willReturn(TOTAL_QUANTITY_MAX_COUNT);

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> totalQuantityValidationService.validateCreatable(
                        sessionalPlanId,
                        SUBJECT_NAME
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(TOTAL_QUANTITY_LIMIT_EXCEEDED);

        then(totalQuantityRepository).should()
                .existsBySessionalPlanIdAndSubjectName(sessionalPlanId, SUBJECT_NAME);
        then(totalQuantityRepository).should().countBySessionalPlanId(sessionalPlanId);
    }

    @Test
    void 세션_총량을_수정할_수_있는지_검증한다() {
        Long sessionalPlanId = 1L;
        Long totalQuantityId = 2L;
        given(totalQuantityRepository.existsBySessionalPlanIdAndSubjectNameAndTotalQuantityIdNot(
                sessionalPlanId,
                SUBJECT_NAME,
                totalQuantityId
        )).willReturn(false);

        totalQuantityValidationService.validateUpdatable(totalQuantityId, sessionalPlanId, SUBJECT_NAME);

        then(totalQuantityRepository).should().existsBySessionalPlanIdAndSubjectNameAndTotalQuantityIdNot(
                sessionalPlanId,
                SUBJECT_NAME,
                totalQuantityId
        );
    }

    @Test
    void 같은_과목명의_다른_세션_총량이_있으면_수정할_수_없다() {
        Long sessionalPlanId = 1L;
        Long totalQuantityId = 2L;
        given(totalQuantityRepository.existsBySessionalPlanIdAndSubjectNameAndTotalQuantityIdNot(
                sessionalPlanId,
                SUBJECT_NAME,
                totalQuantityId
        )).willReturn(true);

        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> totalQuantityValidationService.validateUpdatable(
                        totalQuantityId,
                        sessionalPlanId,
                        SUBJECT_NAME
                ))
                .extracting(ConflictException::getError)
                .isEqualTo(SUBJECT_NAME_ALREADY_EXISTS);

        then(totalQuantityRepository).should().existsBySessionalPlanIdAndSubjectNameAndTotalQuantityIdNot(
                sessionalPlanId,
                SUBJECT_NAME,
                totalQuantityId
        );
    }
}
