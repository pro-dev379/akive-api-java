package net.akive.api.plan.application.service.totalquantity;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.totalquantity.dto.DailyAmountDelayCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.DailyCompletedAmountUpdateCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.DailyAmountUpdateCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityCreateCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityDeleteCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityUpdateCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyAmountDelayCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyQuantityAmountUpdateCommand;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.domain.model.DailyQuantity;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.model.WeeklyQuantity;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.QuantityAmount;
import net.akive.api.plan.domain.value.QuantityMemo;
import net.akive.api.plan.domain.value.Subject;
import net.akive.api.plan.domain.value.TotalQuantityAmount;
import net.akive.api.plan.domain.value.WeekPeriod;
import net.akive.api.plan.domain.value.WeeklyQuantityAmount;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static net.akive.api.common.exception.business.BadRequestError.DAILY_AMOUNT_INCREASE_NOT_ALLOWED;
import static net.akive.api.common.exception.business.BadRequestError.DAILY_DELAY_AMOUNT_EXCEEDS_CURRENT_DAILY_DELAYED_AMOUNT;
import static net.akive.api.common.exception.business.BadRequestError.LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED;
import static net.akive.api.common.exception.business.BadRequestError.WEEKLY_DELAY_AMOUNT_EXCEEDS_CURRENT_WEEKLY_DELAYED_AMOUNT;
import static net.akive.api.common.exception.business.ConflictError.DAILY_AMOUNT_TO_COMPLETE_EXCEEDS_DELAYED_AMOUNT;
import static net.akive.api.common.exception.business.ConflictError.TOTAL_AMOUNT_LESS_THAN_COMPLETED_AMOUNT;
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
        Subject targetSubject = new Subject("영어", "공부");
        TotalQuantityAmount targetTotalQuantityAmount = TotalQuantityAmount.of(130, "페이지", "문법");
        totalQuantity.update(targetSubject, targetTotalQuantityAmount);
        TotalQuantityUpdateCommand command = new TotalQuantityUpdateCommand(
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetSubject,
                targetTotalQuantityAmount
        );

        given(totalQuantityDomainService.update(
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetSubject,
                targetTotalQuantityAmount
        )).willReturn(totalQuantity);

        totalQuantityCommandService.update(command);

        then(totalQuantityDomainService).should().update(
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetSubject,
                targetTotalQuantityAmount
        );
        then(totalQuantityRepository).should().save(totalQuantity);
    }

    @Test
    void 세션_총량_수정_중_도메인_서비스에서_예외가_발생하면_저장하지_않는다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Subject targetSubject = createSubject();
        TotalQuantityAmount targetTotalQuantityAmount = TotalQuantityAmount.of(20, QUANTITY_UNIT);
        TotalQuantityUpdateCommand command = new TotalQuantityUpdateCommand(
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetSubject,
                targetTotalQuantityAmount
        );
        ConflictException exception = new ConflictException(TOTAL_AMOUNT_LESS_THAN_COMPLETED_AMOUNT);

        willThrow(exception)
                .given(totalQuantityDomainService)
                .update(
                        totalQuantityId,
                        sessionalPlanId,
                        userId,
                        targetSubject,
                        targetTotalQuantityAmount
                );

        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> totalQuantityCommandService.update(command))
                .isSameAs(exception);

        then(totalQuantityDomainService).should().update(
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetSubject,
                targetTotalQuantityAmount
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
        WeeklyQuantityAmount targetWeeklyQuantityAmount = WeeklyQuantityAmount.of(20, "첫 주 조정");
        WeeklyQuantityAmountUpdateCommand command = new WeeklyQuantityAmountUpdateCommand(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetWeeklyQuantityAmount
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.of(totalQuantity));

        totalQuantityCommandService.updateWeeklyQuantityAmount(command);

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
        WeeklyQuantityAmount targetWeeklyQuantityAmount = WeeklyQuantityAmount.of(20);
        WeeklyQuantityAmountUpdateCommand command = new WeeklyQuantityAmountUpdateCommand(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetWeeklyQuantityAmount
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.updateWeeklyQuantityAmount(command))
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
        WeeklyQuantityAmount targetWeeklyQuantityAmount = WeeklyQuantityAmount.of(20);
        WeeklyQuantityAmountUpdateCommand command = new WeeklyQuantityAmountUpdateCommand(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetWeeklyQuantityAmount
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.updateWeeklyQuantityAmount(command))
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
        WeeklyQuantityAmount targetWeeklyQuantityAmount = WeeklyQuantityAmount.of(20);
        WeeklyQuantityAmountUpdateCommand command = new WeeklyQuantityAmountUpdateCommand(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetWeeklyQuantityAmount
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.of(totalQuantity));

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> totalQuantityCommandService.updateWeeklyQuantityAmount(command))
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
        Long receivingWeeklyQuantityId = 3L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(totalQuantityId, sessionalPlan, 30, 40, 30);
        WeeklyAmountDelayCommand command = new WeeklyAmountDelayCommand(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingWeeklyQuantityId,
                new QuantityAmount(10)
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.of(totalQuantity));

        totalQuantityCommandService.delayWeeklyAmountTo(command);

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
        WeeklyAmountDelayCommand command = WeeklyAmountDelayCommand.of(
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
                .isThrownBy(() -> totalQuantityCommandService.delayWeeklyAmountTo(command))
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
        WeeklyAmountDelayCommand command = WeeklyAmountDelayCommand.of(
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
                .isThrownBy(() -> totalQuantityCommandService.delayWeeklyAmountTo(command))
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
        Long receivingWeeklyQuantityId = 2L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(totalQuantityId, sessionalPlan, 100, 0, 0);
        totalQuantity.updateWeeklyQuantityAmount(weeklyQuantityId, WeeklyQuantityAmount.of(30));
        WeeklyAmountDelayCommand command = WeeklyAmountDelayCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingWeeklyQuantityId,
                80
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.of(totalQuantity));

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> totalQuantityCommandService.delayWeeklyAmountTo(command))
                .extracting(BadRequestException::getError)
                .isEqualTo(WEEKLY_DELAY_AMOUNT_EXCEEDS_CURRENT_WEEKLY_DELAYED_AMOUNT);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 일일_딜레이를_같은_주의_다른_날로_옮긴다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 1L;
        Long dailyQuantityId = 1L;
        Long receivingDailyQuantityId = 2L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createTotalQuantityWithDailyAmounts(totalQuantityId, sessionalPlan, 40, 60);
        DailyAmountDelayCommand command = new DailyAmountDelayCommand(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingDailyQuantityId,
                new QuantityAmount(10)
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.of(totalQuantity));

        totalQuantityCommandService.delayDailyAmountTo(command);

        WeeklyQuantity weeklyQuantity = totalQuantity.getWeeklyQuantities().getFirst();
        assertDailyQuantityAmount(weeklyQuantity.getDailyQuantities().getFirst(), 30);
        assertDailyQuantityAmount(weeklyQuantity.getDailyQuantities().get(1), 70);
        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId);
        then(totalQuantityRepository).should().save(totalQuantity);
    }

    @Test
    void 세셔널_플랜이_없으면_일일_딜레이를_옮길_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        DailyAmountDelayCommand command = DailyAmountDelayCommand.of(
                1L,
                1L,
                totalQuantityId,
                sessionalPlanId,
                userId,
                2L,
                10
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.delayDailyAmountTo(command))
                .extracting(NotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should(never())
                .findBySessionalPlanIdAndTotalQuantityId(any(), any());
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 세션_총량이_없으면_일일_딜레이를_옮길_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        DailyAmountDelayCommand command = DailyAmountDelayCommand.of(
                1L,
                1L,
                totalQuantityId,
                sessionalPlanId,
                userId,
                2L,
                10
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.delayDailyAmountTo(command))
                .extracting(NotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 일일_딜레이_이동에_실패하면_저장하지_않는다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 1L;
        Long dailyQuantityId = 1L;
        Long receivingDailyQuantityId = 2L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createTotalQuantityWithCompletedDailyAmount(
                totalQuantityId,
                sessionalPlan,
                40,
                10,
                60
        );
        DailyAmountDelayCommand command = DailyAmountDelayCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingDailyQuantityId,
                40
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.of(totalQuantity));

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> totalQuantityCommandService.delayDailyAmountTo(command))
                .extracting(BadRequestException::getError)
                .isEqualTo(DAILY_DELAY_AMOUNT_EXCEEDS_CURRENT_DAILY_DELAYED_AMOUNT);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 일일_계획량을_수정한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 1L;
        Long dailyQuantityId = 1L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createTotalQuantityWithDailyAmounts(totalQuantityId, sessionalPlan, 40, 60);
        QuantityMemo targetDailyMemo = new QuantityMemo("첫날 조정");
        DailyAmountUpdateCommand command = new DailyAmountUpdateCommand(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                new QuantityAmount(20),
                targetDailyMemo
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.of(totalQuantity));

        totalQuantityCommandService.updateDailyAmount(command);

        WeeklyQuantity weeklyQuantity = totalQuantity.getWeeklyQuantities().getFirst();
        assertDailyQuantityAmount(weeklyQuantity.getDailyQuantities().getFirst(), 20);
        assertDailyQuantityAmount(weeklyQuantity.getDailyQuantities().get(1), 80);
        assertThat(weeklyQuantity.getDailyQuantities()
                .getFirst()
                .getDailyQuantityAmount()
                .quantityMemo())
                .isEqualTo(targetDailyMemo);
        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId);
        then(totalQuantityRepository).should().save(totalQuantity);
    }

    @Test
    void 세셔널_플랜이_없으면_일일_계획량을_수정할_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                1L,
                1L,
                totalQuantityId,
                sessionalPlanId,
                userId,
                20,
                "첫날 조정"
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.updateDailyAmount(command))
                .extracting(NotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should(never())
                .findBySessionalPlanIdAndTotalQuantityId(any(), any());
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 세션_총량이_없으면_일일_계획량을_수정할_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                1L,
                1L,
                totalQuantityId,
                sessionalPlanId,
                userId,
                20,
                "첫날 조정"
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.updateDailyAmount(command))
                .extracting(NotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 일일_계획량_수정에_실패하면_저장하지_않는다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 1L;
        Long dailyQuantityId = 1L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createTotalQuantityWithDailyAmounts(totalQuantityId, sessionalPlan, 40, 60);
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                50,
                "첫날 조정"
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.of(totalQuantity));

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> totalQuantityCommandService.updateDailyAmount(command))
                .extracting(BadRequestException::getError)
                .isEqualTo(DAILY_AMOUNT_INCREASE_NOT_ALLOWED);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 일일_계획_달성량을_수정한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 1L;
        Long dailyQuantityId = 1L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createTotalQuantityWithCompletedDailyAmount(
                totalQuantityId,
                sessionalPlan,
                40,
                10,
                60
        );
        DailyCompletedAmountUpdateCommand command = DailyCompletedAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                30
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.of(totalQuantity));

        totalQuantityCommandService.updateDailyCompletedAmount(command);

        WeeklyQuantity weeklyQuantity = totalQuantity.getWeeklyQuantities().getFirst();
        assertDailyQuantityAmount(weeklyQuantity.getDailyQuantities().getFirst(), 40, 30, 10);
        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId);
        then(totalQuantityRepository).should().save(totalQuantity);
    }

    @Test
    void 세셔널_플랜이_없으면_일일_계획_달성량을_수정할_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        DailyCompletedAmountUpdateCommand command = DailyCompletedAmountUpdateCommand.of(
                1L,
                1L,
                totalQuantityId,
                sessionalPlanId,
                userId,
                30
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.updateDailyCompletedAmount(command))
                .extracting(NotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should(never())
                .findBySessionalPlanIdAndTotalQuantityId(any(), any());
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 세션_총량이_없으면_일일_계획_달성량을_수정할_수_없다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        DailyCompletedAmountUpdateCommand command = DailyCompletedAmountUpdateCommand.of(
                1L,
                1L,
                totalQuantityId,
                sessionalPlanId,
                userId,
                30
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.updateDailyCompletedAmount(command))
                .extracting(NotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 일일_계획_달성량_수정에_실패하면_저장하지_않는다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 1L;
        Long dailyQuantityId = 1L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createTotalQuantityWithCompletedDailyAmount(
                totalQuantityId,
                sessionalPlan,
                40,
                10,
                60
        );
        DailyCompletedAmountUpdateCommand command = DailyCompletedAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                50
        );

        given(sessionalPlanRepository.existsBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(true);
        given(totalQuantityRepository.findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId))
                .willReturn(Optional.of(totalQuantity));

        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> totalQuantityCommandService.updateDailyCompletedAmount(command))
                .extracting(ConflictException::getError)
                .isEqualTo(DAILY_AMOUNT_TO_COMPLETE_EXCEEDS_DELAYED_AMOUNT);

        then(sessionalPlanRepository).should().existsBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId);
        then(totalQuantityRepository).should(never()).save(any());
    }

    private void assertDailyQuantityAmount(DailyQuantity dailyQuantity, int expectedDailyAmount) {
        DailyQuantityAmount dailyQuantityAmount = dailyQuantity.getDailyQuantityAmount();

        assertThat(dailyQuantityAmount.quantityAmount().value()).isEqualTo(expectedDailyAmount);
        assertThat(dailyQuantityAmount.completedQuantityAmount().value()).isZero();
        assertThat(dailyQuantityAmount.delayedQuantityAmount().value()).isEqualTo(expectedDailyAmount);
    }

    private void assertDailyQuantityAmount(
            DailyQuantity dailyQuantity,
            int expectedDailyAmount,
            int expectedDailyCompletedAmount,
            int expectedDailyDelayedAmount
    ) {
        DailyQuantityAmount dailyQuantityAmount = dailyQuantity.getDailyQuantityAmount();

        assertThat(dailyQuantityAmount.quantityAmount().value()).isEqualTo(expectedDailyAmount);
        assertThat(dailyQuantityAmount.completedQuantityAmount().value()).isEqualTo(expectedDailyCompletedAmount);
        assertThat(dailyQuantityAmount.delayedQuantityAmount().value()).isEqualTo(expectedDailyDelayedAmount);
    }

    private TotalQuantity createTotalQuantityWithCompletedDailyAmount(
            Long totalQuantityId,
            SessionalPlan sessionalPlan,
            int firstDailyAmount,
            int firstCompletedDailyAmount,
            int secondDailyAmount
    ) {
        return TotalQuantity.reconstruct(
                totalQuantityId,
                sessionalPlan,
                createSubject(),
                TotalQuantityAmount.of(firstDailyAmount + secondDailyAmount, QUANTITY_UNIT),
                List.of(
                        WeeklyQuantity.reconstruct(
                                1L,
                                new WeekPeriod(
                                        LocalDate.of(2026, 1, 1),
                                        LocalDate.of(2026, 1, 4)
                                ),
                                WeeklyQuantityAmount.of(firstDailyAmount + secondDailyAmount),
                                List.of(
                                        DailyQuantity.reconstruct(
                                                1L,
                                                LocalDate.of(2026, 1, 1),
                                                DailyQuantityAmount.of(
                                                        firstDailyAmount,
                                                        firstCompletedDailyAmount,
                                                        firstDailyAmount - firstCompletedDailyAmount
                                                )
                                        ),
                                        DailyQuantity.reconstruct(
                                                2L,
                                                LocalDate.of(2026, 1, 2),
                                                DailyQuantityAmount.of(secondDailyAmount, 0, secondDailyAmount)
                                        )
                                )
                        ),
                        createWeeklyQuantity(
                                2L,
                                LocalDate.of(2026, 1, 5),
                                LocalDate.of(2026, 1, 11),
                                0
                        ),
                        createWeeklyQuantity(
                                3L,
                                LocalDate.of(2026, 1, 12),
                                LocalDate.of(2026, 1, 12),
                                0
                        )
                )
        );
    }

    private TotalQuantity createTotalQuantityWithDailyAmounts(
            Long totalQuantityId,
            SessionalPlan sessionalPlan,
            int firstDailyAmount,
            int secondDailyAmount
    ) {
        return TotalQuantity.reconstruct(
                totalQuantityId,
                sessionalPlan,
                createSubject(),
                TotalQuantityAmount.of(firstDailyAmount + secondDailyAmount, QUANTITY_UNIT),
                List.of(
                        WeeklyQuantity.reconstruct(
                                1L,
                                new WeekPeriod(
                                        LocalDate.of(2026, 1, 1),
                                        LocalDate.of(2026, 1, 4)
                                ),
                                WeeklyQuantityAmount.of(firstDailyAmount + secondDailyAmount),
                                List.of(
                                        DailyQuantity.reconstruct(
                                                1L,
                                                LocalDate.of(2026, 1, 1),
                                                DailyQuantityAmount.of(firstDailyAmount, 0, firstDailyAmount)
                                        ),
                                        DailyQuantity.reconstruct(
                                                2L,
                                                LocalDate.of(2026, 1, 2),
                                                DailyQuantityAmount.of(secondDailyAmount, 0, secondDailyAmount)
                                        )
                                )
                        ),
                        createWeeklyQuantity(
                                2L,
                                LocalDate.of(2026, 1, 5),
                                LocalDate.of(2026, 1, 11),
                                0
                        ),
                        createWeeklyQuantity(
                                3L,
                                LocalDate.of(2026, 1, 12),
                                LocalDate.of(2026, 1, 12),
                                0
                        )
                )
        );
    }
}
