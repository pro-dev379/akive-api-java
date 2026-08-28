package net.akive.api.plan.application.service.totalquantity;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityCreateCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityDeleteCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityUpdateCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyQuantityDelayCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyQuantityReduceCommand;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.value.QuantityAmount;
import net.akive.api.plan.domain.value.Subject;
import net.akive.api.plan.domain.value.TotalQuantityAmount;
import net.akive.api.plan.domain.value.WeeklyQuantityAmount;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.BadRequestError.LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED;
import static net.akive.api.common.exception.business.BadRequestError.WEEKLY_DELAY_QUANTITY_EXCEEDS_CURRENT_WEEKLY_DELAYED_QUANTITY;
import static net.akive.api.common.exception.business.ConflictError.TOTAL_QUANTITY_LESS_THAN_COMPLETED_QUANTITY;
import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.NotFoundError.TOTAL_QUANTITY_NOT_FOUND;
import static net.akive.api.plan.domain.PlanAssertions.assertWeeklyQuantityAmounts;
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
        totalQuantity.update(subject, totalQuantityAmount);
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
        )).willReturn(totalQuantity);

        totalQuantityCommandService.update(command);

        then(totalQuantityDomainService).should().update(
                totalQuantityId,
                sessionalPlanId,
                userId,
                subject,
                totalQuantityAmount
        );
        then(totalQuantityRepository).should().save(totalQuantity);
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

    @Test
    void 주간_계획량을_줄인다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 1L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(totalQuantityId, sessionalPlan, 30, 40, 30);
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(20, "첫 주 조정");
        WeeklyQuantityReduceCommand command = new WeeklyQuantityReduceCommand(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                weeklyQuantityAmount
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.of(totalQuantity));

        totalQuantityCommandService.reduceWeeklyQuantity(command);

        assertWeeklyQuantityAmounts(totalQuantity, 20, 50, 30);
        assertThat(totalQuantity.getWeeklyQuantities()
                .getFirst()
                .getWeeklyQuantityAmount()
                .quantityMemo()
                .value())
                .isEqualTo("첫 주 조정");

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId);
        then(totalQuantityRepository).should().save(totalQuantity);
    }

    @Test
    void 세셔널_플랜이_없으면_주간_계획량을_줄일_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 1L;
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(20, null);
        WeeklyQuantityReduceCommand command = new WeeklyQuantityReduceCommand(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                weeklyQuantityAmount
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.reduceWeeklyQuantity(command))
                .extracting(NotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should(never())
                .findBySessionalPlanIdAndTotalQuantityId(any(), any());
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 세션_총량이_없으면_주간_계획량을_줄일_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 1L;
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(20, null);
        WeeklyQuantityReduceCommand command = new WeeklyQuantityReduceCommand(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                weeklyQuantityAmount
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.reduceWeeklyQuantity(command))
                .extracting(NotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 주간_계획량_감소에_실패하면_저장하지_않는다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 3L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(totalQuantityId, sessionalPlan, 30, 40, 30);
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(20, null);
        WeeklyQuantityReduceCommand command = new WeeklyQuantityReduceCommand(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                weeklyQuantityAmount
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.of(totalQuantity));

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> totalQuantityCommandService.reduceWeeklyQuantity(command))
                .extracting(BadRequestException::getError)
                .isEqualTo(LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 주간_딜레이를_다른_주로_옮긴다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 1L;
        Long targetWeeklyQuantityId = 3L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(totalQuantityId, sessionalPlan, 30, 40, 30);
        WeeklyQuantityDelayCommand command = new WeeklyQuantityDelayCommand(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetWeeklyQuantityId,
                new QuantityAmount(10)
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.of(totalQuantity));

        totalQuantityCommandService.delayWeeklyQuantity(command);

        assertWeeklyQuantityAmounts(totalQuantity, 20, 40, 40);
        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId);
        then(totalQuantityRepository).should().save(totalQuantity);
    }

    @Test
    void 세셔널_플랜이_없으면_주간_딜레이를_옮길_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        WeeklyQuantityDelayCommand command = WeeklyQuantityDelayCommand.of(
                1L,
                totalQuantityId,
                sessionalPlanId,
                userId,
                3L,
                10
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.delayWeeklyQuantity(command))
                .extracting(NotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should(never())
                .findBySessionalPlanIdAndTotalQuantityId(any(), any());
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 세션_총량이_없으면_주간_딜레이를_옮길_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        WeeklyQuantityDelayCommand command = WeeklyQuantityDelayCommand.of(
                1L,
                totalQuantityId,
                sessionalPlanId,
                userId,
                3L,
                10
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.delayWeeklyQuantity(command))
                .extracting(NotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 주간_딜레이_이동에_실패하면_저장하지_않는다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 1L;
        Long targetWeeklyQuantityId = 2L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(totalQuantityId, sessionalPlan, 100, 0, 0);
        totalQuantity.reduceWeeklyQuantity(weeklyQuantityId, WeeklyQuantityAmount.of(30, null));
        WeeklyQuantityDelayCommand command = WeeklyQuantityDelayCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetWeeklyQuantityId,
                80
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.of(totalQuantity));

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> totalQuantityCommandService.delayWeeklyQuantity(command))
                .extracting(BadRequestException::getError)
                .isEqualTo(WEEKLY_DELAY_QUANTITY_EXCEEDS_CURRENT_WEEKLY_DELAYED_QUANTITY);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId);
        then(totalQuantityRepository).should(never()).save(any());
    }
}
