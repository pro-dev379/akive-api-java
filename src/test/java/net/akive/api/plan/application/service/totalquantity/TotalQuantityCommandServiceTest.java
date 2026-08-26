package net.akive.api.plan.application.service.totalquantity;

import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityCreateCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityDeleteCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityUpdateCommand;
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

import static net.akive.api.common.exception.business.ConflictError.TOTAL_QUANTITY_LESS_THAN_COMPLETED_QUANTITY;
import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.plan.domain.PlanFixture.*;
import static net.akive.api.user.domain.UserFixture.createPersistedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TotalQuantityCommandServiceTest {

    @Mock
    SessionalPlanRepository sessionalPlanRepository;

    @Mock
    TotalQuantityRepository totalQuantityRepository;

    @Mock
    TotalQuantityDomainService totalQuantityDomainService;

    @InjectMocks
    TotalQuantityCommandService totalQuantityCommandService;

    @Test
    void 세션_총량을_생성한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        Subject subject = createSubject();
        TotalQuantityAmount totalQuantityAmount = createTotalQuantityAmount();
        TotalQuantity totalQuantity = createTotalQuantity(sessionalPlan);
        TotalQuantity savedTotalQuantity = TotalQuantity.reconstruct(
                totalQuantityId,
                sessionalPlan,
                subject,
                totalQuantityAmount,
                totalQuantity.getWeeklyQuantities()
        );
        TotalQuantityCreateCommand command = new TotalQuantityCreateCommand(
                sessionalPlanId,
                userId,
                subject,
                totalQuantityAmount
        );

        given(totalQuantityDomainService.create(sessionalPlanId, userId, subject, totalQuantityAmount))
                .willReturn(totalQuantity);
        given(totalQuantityRepository.save(totalQuantity))
                .willReturn(savedTotalQuantity);

        Long result = totalQuantityCommandService.create(command);

        assertThat(result).isEqualTo(totalQuantityId);

        then(totalQuantityDomainService).should().create(sessionalPlanId, userId, subject, totalQuantityAmount);
        then(totalQuantityRepository).should().save(totalQuantity);
    }

    @Test
    void 세션_총량_생성_중_도메인_서비스에서_예외가_발생하면_저장하지_않는다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Subject subject = createSubject();
        TotalQuantityAmount totalQuantityAmount = createTotalQuantityAmount();
        TotalQuantityCreateCommand command = new TotalQuantityCreateCommand(
                sessionalPlanId,
                userId,
                subject,
                totalQuantityAmount
        );
        NotFoundException exception = new NotFoundException(SESSIONAL_PLAN_NOT_FOUND);

        given(totalQuantityDomainService.create(sessionalPlanId, userId, subject, totalQuantityAmount))
                .willThrow(exception);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.create(command))
                .isSameAs(exception);

        then(totalQuantityDomainService).should().create(sessionalPlanId, userId, subject, totalQuantityAmount);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 세션_총량을_수정한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createPersistedTotalQuantity(totalQuantityId, sessionalPlan);
        Subject subject = new Subject("영어", "공부");
        TotalQuantityAmount totalQuantityAmount = TotalQuantityAmount.of(130, "페이지", "문법");
        TotalQuantity updatedTotalQuantity = totalQuantity.update(subject, totalQuantityAmount);
        TotalQuantityUpdateCommand command = new TotalQuantityUpdateCommand(
                totalQuantityId,
                sessionalPlanId,
                userId,
                subject,
                totalQuantityAmount
        );

        given(totalQuantityDomainService.update(
                totalQuantityId,
                sessionalPlanId,
                userId,
                subject,
                totalQuantityAmount
        )).willReturn(updatedTotalQuantity);

        totalQuantityCommandService.update(command);

        then(totalQuantityDomainService).should().update(
                totalQuantityId,
                sessionalPlanId,
                userId,
                subject,
                totalQuantityAmount
        );
        then(totalQuantityRepository).should().save(updatedTotalQuantity);
    }

    @Test
    void 세션_총량_수정_중_도메인_서비스에서_예외가_발생하면_저장하지_않는다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Subject subject = createSubject();
        TotalQuantityAmount totalQuantityAmount = TotalQuantityAmount.of(20, QUANTITY_UNIT, null);
        TotalQuantityUpdateCommand command = new TotalQuantityUpdateCommand(
                totalQuantityId,
                sessionalPlanId,
                userId,
                subject,
                totalQuantityAmount
        );
        ConflictException exception = new ConflictException(TOTAL_QUANTITY_LESS_THAN_COMPLETED_QUANTITY);

        willThrow(exception)
                .given(totalQuantityDomainService)
                .update(
                        totalQuantityId,
                        sessionalPlanId,
                        userId,
                        subject,
                        totalQuantityAmount
                );

        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> totalQuantityCommandService.update(command))
                .isSameAs(exception);

        then(totalQuantityDomainService).should().update(
                totalQuantityId,
                sessionalPlanId,
                userId,
                subject,
                totalQuantityAmount
        );
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 세션_총량을_삭제한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        TotalQuantityDeleteCommand command = new TotalQuantityDeleteCommand(
                sessionalPlanId,
                userId,
                totalQuantityId
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);

        totalQuantityCommandService.delete(command);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .deleteByTotalQuantityIdAndSessionalPlanId(totalQuantityId, sessionalPlanId);
    }

    @Test
    void 세셔널_플랜이_없으면_세션_총량_삭제에_실패하고_예외를_던진다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        TotalQuantityDeleteCommand command = new TotalQuantityDeleteCommand(
                sessionalPlanId,
                userId,
                totalQuantityId
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.delete(command))
                .extracting(NotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should(never())
                .deleteByTotalQuantityIdAndSessionalPlanId(totalQuantityId, sessionalPlanId);
    }
}
