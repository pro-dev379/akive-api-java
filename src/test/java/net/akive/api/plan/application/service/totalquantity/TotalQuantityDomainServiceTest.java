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
    TotalQuantityRepository totalQuantityRepository;

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
    void 세션_총량을_수정한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createPersistedTotalQuantity(totalQuantityId, sessionalPlan);
        Subject targetSubject = new Subject("영어", "공부");
        TotalQuantityAmount targetTotalQuantityAmount = TotalQuantityAmount.of(130, "페이지", "문법");

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                sessionalPlanId,
                userId,
                totalQuantityId
        )).willReturn(Optional.of(totalQuantity));

        TotalQuantity updatedTotalQuantity = totalQuantityDomainService.update(
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetSubject,
                targetTotalQuantityAmount
        );

        assertThat(updatedTotalQuantity.getTotalQuantityId()).isEqualTo(totalQuantityId);
        assertThat(updatedTotalQuantity.getSubject()).isEqualTo(targetSubject);
        assertThat(updatedTotalQuantity.getTotalQuantityAmount()).isEqualTo(targetTotalQuantityAmount);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(sessionalPlanId, userId, totalQuantityId);
        then(totalQuantityValidationService).should()
                .validateUpdatable(totalQuantityId, sessionalPlanId, targetSubject.name());
    }

    @Test
    void 세셔널_플랜이_없으면_세션_총량을_생성할_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Subject subject = createSubject();
        TotalQuantityAmount totalQuantityAmount = createTotalQuantityAmount();

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityDomainService.create(
                        sessionalPlanId,
                        userId,
                        subject,
                        totalQuantityAmount
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should().findBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityValidationService).should(never()).validateCreatable(sessionalPlanId, subject.name());
    }

    @Test
    void 세셔널_플랜이_없으면_세션_총량을_수정할_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Subject targetSubject = createSubject();
        TotalQuantityAmount targetTotalQuantityAmount = createTotalQuantityAmount();

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityDomainService.update(
                        totalQuantityId,
                        sessionalPlanId,
                        userId,
                        targetSubject,
                        targetTotalQuantityAmount
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(sessionalPlanId, userId, totalQuantityId);
        then(totalQuantityValidationService).should(never())
                .validateUpdatable(any(), any(), any());
    }

    @Test
    void 세션_총량이_없으면_세션_총량을_수정할_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Subject targetSubject = createSubject();
        TotalQuantityAmount targetTotalQuantityAmount = createTotalQuantityAmount();

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                sessionalPlanId,
                userId,
                totalQuantityId
        )).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityDomainService.update(
                        totalQuantityId,
                        sessionalPlanId,
                        userId,
                        targetSubject,
                        targetTotalQuantityAmount
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(sessionalPlanId, userId, totalQuantityId);
        then(totalQuantityValidationService).should(never())
                .validateUpdatable(any(), any(), any());
    }

    @Test
    void 생성_가능_검증에_실패하면_세션_총량을_생성할_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        Subject subject = createSubject();
        TotalQuantityAmount totalQuantityAmount = createTotalQuantityAmount();
        ResourceConflictException exception = new ResourceConflictException(SUBJECT_NAME_ALREADY_EXISTS);

        given(sessionalPlanRepository.findBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionalPlan));
        willThrow(exception)
                .given(totalQuantityValidationService)
                .validateCreatable(sessionalPlanId, subject.name());

        assertThatExceptionOfType(ResourceConflictException.class)
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

    @Test
    void 수정_가능_검증에_실패하면_세션_총량을_수정할_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createPersistedTotalQuantity(totalQuantityId, sessionalPlan);
        Subject targetSubject = createSubject();
        TotalQuantityAmount targetTotalQuantityAmount = createTotalQuantityAmount();
        ResourceConflictException exception = new ResourceConflictException(SUBJECT_NAME_ALREADY_EXISTS);

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                sessionalPlanId,
                userId,
                totalQuantityId
        )).willReturn(Optional.of(totalQuantity));
        willThrow(exception)
                .given(totalQuantityValidationService)
                .validateUpdatable(totalQuantityId, sessionalPlanId, targetSubject.name());

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> totalQuantityDomainService.update(
                        totalQuantityId,
                        sessionalPlanId,
                        userId,
                        targetSubject,
                        targetTotalQuantityAmount
                ))
                .isSameAs(exception);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(sessionalPlanId, userId, totalQuantityId);
        then(totalQuantityValidationService).should()
                .validateUpdatable(totalQuantityId, sessionalPlanId, targetSubject.name());
    }
}
