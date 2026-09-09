package net.akive.api.plan.application.service.totalquantity;

import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.value.Subject;
import net.akive.api.plan.domain.value.TotalQuantityAmount;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceConflictError.SUBJECT_NAME_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.TOTAL_QUANTITY_NOT_FOUND;
import static net.akive.api.plan.domain.assertion.TotalQuantityAssert.assertThatTotalQuantity;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.SESSIONAL_PLAN_ID;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.createReconstructedSessionalPlan;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.*;
import static net.akive.api.user.domain.UserFixture.USER_ID;
import static net.akive.api.user.domain.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TotalQuantityDomainServiceTest {

    @Mock
    SessionalPlanRepository sessionalPlanRepository;

    @Mock
    TotalQuantityRepository totalQuantityRepository;

    @Mock
    TotalQuantityValidationService totalQuantityValidationService;

    @InjectMocks
    TotalQuantityDomainService totalQuantityDomainService;

    @Test
    void 세션_총량을_생성한다() {
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, user);
        Subject subject = createSubject();
        TotalQuantityAmount totalQuantityAmount = createTotalQuantityAmount();

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(SESSIONAL_PLAN_ID, USER_ID))
                .willReturn(Optional.of(sessionalPlan));

        TotalQuantity totalQuantity = totalQuantityDomainService.create(
                SESSIONAL_PLAN_ID,
                USER_ID,
                subject,
                totalQuantityAmount
        );

        assertThat(totalQuantity.getSessionalPlan()).isEqualTo(sessionalPlan);
        assertThat(totalQuantity.getSubject()).isEqualTo(subject);
        assertThat(totalQuantity.getTotalQuantityAmount()).isEqualTo(totalQuantityAmount);
        assertThatTotalQuantity(totalQuantity).hasWeeklyAmounts(TOTAL_AMOUNT, 0, 0);

        then(sessionalPlanRepository).should().findBySessionalPlanIdAndUserId(SESSIONAL_PLAN_ID, USER_ID);
        then(totalQuantityValidationService).should().validateCreatable(SESSIONAL_PLAN_ID, subject.name());
    }

    @Test
    void 세션_총량을_수정한다() {
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, user);
        TotalQuantity totalQuantity = createReconstructedTotalQuantity(FIRST_TOTAL_QUANTITY_ID, sessionalPlan);

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.of(totalQuantity));

        TotalQuantity updatedTotalQuantity = totalQuantityDomainService.update(
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TARGET_SUBJECT,
                TARGET_TOTAL_QUANTITY_AMOUNT
        );

        assertThat(updatedTotalQuantity.getTotalQuantityId()).isEqualTo(FIRST_TOTAL_QUANTITY_ID);
        assertThat(updatedTotalQuantity.getSubject()).isEqualTo(TARGET_SUBJECT);
        assertThat(updatedTotalQuantity.getTotalQuantityAmount()).isEqualTo(TARGET_TOTAL_QUANTITY_AMOUNT);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
        then(totalQuantityValidationService).should()
                .validateUpdatable(FIRST_TOTAL_QUANTITY_ID, SESSIONAL_PLAN_ID, TARGET_SUBJECT.name());
    }

    @Test
    void 세셔널_플랜이_없으면_세션_총량을_생성할_수_없다() {
        Subject subject = createSubject();
        TotalQuantityAmount totalQuantityAmount = createTotalQuantityAmount();

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(SESSIONAL_PLAN_ID, USER_ID))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityDomainService.create(
                        SESSIONAL_PLAN_ID,
                        USER_ID,
                        subject,
                        totalQuantityAmount
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should().findBySessionalPlanIdAndUserId(SESSIONAL_PLAN_ID, USER_ID);
        then(totalQuantityValidationService).should(never()).validateCreatable(SESSIONAL_PLAN_ID, subject.name());
    }

    @Test
    void 세션_총량이_없으면_세션_총량을_수정할_수_없다() {
        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityDomainService.update(
                        FIRST_TOTAL_QUANTITY_ID,
                        SESSIONAL_PLAN_ID,
                        USER_ID,
                        TARGET_SUBJECT,
                        TARGET_TOTAL_QUANTITY_AMOUNT
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
        then(totalQuantityValidationService).should(never())
                .validateUpdatable(any(), any(), any());
    }

    @Test
    void 생성_가능_검증에_실패하면_세션_총량을_생성할_수_없다() {
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, user);
        Subject subject = createSubject();
        TotalQuantityAmount totalQuantityAmount = createTotalQuantityAmount();
        ResourceConflictException exception = new ResourceConflictException(SUBJECT_NAME_ALREADY_EXISTS);

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(SESSIONAL_PLAN_ID, USER_ID))
                .willReturn(Optional.of(sessionalPlan));
        willThrow(exception)
                .given(totalQuantityValidationService)
                .validateCreatable(SESSIONAL_PLAN_ID, subject.name());

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> totalQuantityDomainService.create(
                        SESSIONAL_PLAN_ID,
                        USER_ID,
                        subject,
                        totalQuantityAmount
                ))
                .isSameAs(exception);

        then(sessionalPlanRepository).should().findBySessionalPlanIdAndUserId(SESSIONAL_PLAN_ID, USER_ID);
        then(totalQuantityValidationService).should().validateCreatable(SESSIONAL_PLAN_ID, subject.name());
    }

    @Test
    void 수정_가능_검증에_실패하면_세션_총량을_수정할_수_없다() {
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, user);
        TotalQuantity totalQuantity = createReconstructedTotalQuantity(FIRST_TOTAL_QUANTITY_ID, sessionalPlan);
        ResourceConflictException exception = new ResourceConflictException(SUBJECT_NAME_ALREADY_EXISTS);

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.of(totalQuantity));
        willThrow(exception)
                .given(totalQuantityValidationService)
                .validateUpdatable(FIRST_TOTAL_QUANTITY_ID, SESSIONAL_PLAN_ID, TARGET_SUBJECT.name());

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> totalQuantityDomainService.update(
                        FIRST_TOTAL_QUANTITY_ID,
                        SESSIONAL_PLAN_ID,
                        USER_ID,
                        TARGET_SUBJECT,
                        TARGET_TOTAL_QUANTITY_AMOUNT
                ))
                .isSameAs(exception);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
        then(totalQuantityValidationService).should()
                .validateUpdatable(FIRST_TOTAL_QUANTITY_ID, SESSIONAL_PLAN_ID, TARGET_SUBJECT.name());
    }
}
