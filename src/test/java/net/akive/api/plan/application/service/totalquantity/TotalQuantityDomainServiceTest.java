package net.akive.api.plan.application.service.totalquantity;

import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.required.SessionalPlanRepository;
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

import static net.akive.api.common.exception.business.ConflictError.SUBJECT_NAME_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.plan.domain.PlanFixture.*;
import static net.akive.api.user.domain.UserFixture.createPersistedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TotalQuantityDomainServiceTest {

    @Mock
    SessionalPlanRepository sessionalPlanRepository;

    @Mock
    TotalQuantityValidationService totalQuantityValidationService;

    @InjectMocks
    TotalQuantityDomainService totalQuantityDomainService;

    @Test
    void 세션_총량을_생성한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        Subject subject = createSubject();
        TotalQuantityAmount totalQuantityAmount = createTotalQuantityAmount();

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionalPlan));

        TotalQuantity totalQuantity = totalQuantityDomainService.create(
                sessionalPlanId,
                userId,
                subject,
                totalQuantityAmount
        );

        assertThat(totalQuantity.getSessionalPlan()).isEqualTo(sessionalPlan);
        assertThat(totalQuantity.getSubject()).isEqualTo(subject);
        assertThat(totalQuantity.getTotalQuantityAmount()).isEqualTo(totalQuantityAmount);
        assertThat(totalQuantity.getWeeklyQuantities())
                .hasSize(sessionalPlan.getSessionPeriod().splitIntoWeeks().size());

        then(sessionalPlanRepository).should().findBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityValidationService).should().validateCreatable(sessionalPlanId, subject.name());
    }

    @Test
    void 세셔널_플랜이_없으면_세션_총량을_생성할_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Subject subject = createSubject();
        TotalQuantityAmount totalQuantityAmount = createTotalQuantityAmount();

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityDomainService.create(
                        sessionalPlanId,
                        userId,
                        subject,
                        totalQuantityAmount
                ))
                .extracting(NotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should().findBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityValidationService).should(never()).validateCreatable(sessionalPlanId, subject.name());
    }

    @Test
    void 생성_가능_검증에_실패하면_세션_총량을_생성할_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        Subject subject = createSubject();
        TotalQuantityAmount totalQuantityAmount = createTotalQuantityAmount();
        ConflictException exception = new ConflictException(SUBJECT_NAME_ALREADY_EXISTS);

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionalPlan));
        willThrow(exception)
                .given(totalQuantityValidationService)
                .validateCreatable(sessionalPlanId, subject.name());

        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> totalQuantityDomainService.create(
                        sessionalPlanId,
                        userId,
                        subject,
                        totalQuantityAmount
                ))
                .isSameAs(exception);

        then(sessionalPlanRepository).should().findBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityValidationService).should().validateCreatable(sessionalPlanId, subject.name());
    }
}
