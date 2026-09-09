package net.akive.api.plan.application.service.totalquantity;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.totalquantity.dto.*;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.model.WeeklyQuantity;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.Subject;
import net.akive.api.plan.domain.value.TotalQuantityAmount;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.TOTAL_QUANTITY_NOT_FOUND;
import static net.akive.api.plan.application.fixture.TotalQuantityCommandFixture.*;
import static net.akive.api.plan.domain.assertion.TotalQuantityAssert.assertThatTotalQuantity;
import static net.akive.api.plan.domain.assertion.WeeklyQuantityAssert.assertThatWeeklyQuantity;
import static net.akive.api.plan.domain.fixture.DailyQuantityFixture.TARGET_DAILY_MEMO;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.SESSIONAL_PLAN_ID;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.createReconstructedSessionalPlan;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.*;
import static net.akive.api.plan.domain.fixture.WeeklyQuantityFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TotalQuantityCommandServiceTest {

    @Mock
    TotalQuantityRepository totalQuantityRepository;

    @Mock
    TotalQuantityDomainService totalQuantityDomainService;

    @InjectMocks
    TotalQuantityCommandService totalQuantityCommandService;

    @Test
    void 세션_총량을_생성한다() {
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID);
        Subject subject = createSubject();
        TotalQuantityAmount totalQuantityAmount = createTotalQuantityAmount();
        TotalQuantity totalQuantity = createTotalQuantity(sessionalPlan);
        TotalQuantity savedTotalQuantity = createReconstructedTotalQuantity(FIRST_TOTAL_QUANTITY_ID, totalQuantity);
        TotalQuantityCreateCommand command = createTotalQuantityCreateCommand(subject, totalQuantityAmount);

        given(totalQuantityDomainService.create(
                SESSIONAL_PLAN_ID,
                USER_ID,
                subject,
                totalQuantityAmount
        )).willReturn(totalQuantity);
        given(totalQuantityRepository.save(totalQuantity)).willReturn(savedTotalQuantity);

        Long result = totalQuantityCommandService.create(command);

        assertThat(result).isEqualTo(FIRST_TOTAL_QUANTITY_ID);

        then(totalQuantityDomainService).should().create(SESSIONAL_PLAN_ID, USER_ID, subject, totalQuantityAmount);
        then(totalQuantityRepository).should().save(totalQuantity);
    }

    @Test
    void 세션_총량_생성_중_도메인_서비스에서_예외가_발생하면_저장하지_않는다() {
        Subject subject = createSubject();
        TotalQuantityAmount totalQuantityAmount = createTotalQuantityAmount();
        TotalQuantityCreateCommand command = createTotalQuantityCreateCommand(subject, totalQuantityAmount);
        ResourceNotFoundException exception = new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND);

        given(totalQuantityDomainService.create(SESSIONAL_PLAN_ID, USER_ID, subject, totalQuantityAmount))
                .willThrow(exception);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.create(command))
                .isSameAs(exception);

        then(totalQuantityDomainService).should().create(SESSIONAL_PLAN_ID, USER_ID, subject, totalQuantityAmount);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 세션_총량을_수정한다() {
        TotalQuantity totalQuantity = createReconstructedTotalQuantity(FIRST_TOTAL_QUANTITY_ID);
        TotalQuantityUpdateCommand command =
                createTotalQuantityUpdateCommand(TARGET_SUBJECT, TARGET_TOTAL_QUANTITY_AMOUNT);

        given(totalQuantityDomainService.update(
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TARGET_SUBJECT,
                TARGET_TOTAL_QUANTITY_AMOUNT
        )).willReturn(totalQuantity);

        totalQuantityCommandService.update(command);

        then(totalQuantityDomainService).should().update(
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TARGET_SUBJECT,
                TARGET_TOTAL_QUANTITY_AMOUNT
        );
        then(totalQuantityRepository).should().save(totalQuantity);
    }

    @Test
    void 세션_총량_수정_중_도메인_서비스에서_예외가_발생하면_저장하지_않는다() {
        TotalQuantityUpdateCommand command =
                createTotalQuantityUpdateCommand(TARGET_SUBJECT, TARGET_REDUCED_TOTAL_QUANTITY_AMOUNT);
        BusinessRuleViolationException exception =
                new BusinessRuleViolationException(TOTAL_AMOUNT_LESS_THAN_COMPLETED_AMOUNT);

        willThrow(exception)
                .given(totalQuantityDomainService)
                .update(
                        FIRST_TOTAL_QUANTITY_ID,
                        SESSIONAL_PLAN_ID,
                        USER_ID,
                        TARGET_SUBJECT,
                        TARGET_REDUCED_TOTAL_QUANTITY_AMOUNT
                );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> totalQuantityCommandService.update(command))
                .isSameAs(exception);

        then(totalQuantityDomainService).should().update(
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TARGET_SUBJECT,
                TARGET_REDUCED_TOTAL_QUANTITY_AMOUNT
        );
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 세션_총량을_삭제한다() {
        TotalQuantityDeleteCommand command = createTotalQuantityDeleteCommand(FIRST_TOTAL_QUANTITY_ID);
        totalQuantityCommandService.delete(command);

        then(totalQuantityRepository).should()
                .deleteBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
    }

    @Test
    void 세션_총량이_없으면_삭제에_실패하고_예외를_던진다() {
        TotalQuantityDeleteCommand command = createTotalQuantityDeleteCommand(UNKNOWN_TOTAL_QUANTITY_ID);

        willThrow(new ResourceNotFoundException(TOTAL_QUANTITY_NOT_FOUND))
                .given(totalQuantityRepository)
                .deleteBySessionalPlanIdAndUserIdAndTotalQuantityId(
                        SESSIONAL_PLAN_ID,
                        USER_ID,
                        UNKNOWN_TOTAL_QUANTITY_ID
                );

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.delete(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(totalQuantityRepository).should()
                .deleteBySessionalPlanIdAndUserIdAndTotalQuantityId(
                        SESSIONAL_PLAN_ID,
                        USER_ID,
                        UNKNOWN_TOTAL_QUANTITY_ID
                );
    }

    @Test
    void 주간_계획량을_줄인다() {
        TotalQuantity totalQuantity = createReconstructedTotalQuantityWithWeeklyAmounts(30, 40, 30);
        WeeklyQuantityAmountUpdateCommand command = createWeeklyQuantityAmountUpdateCommand(
                FIRST_WEEKLY_QUANTITY_ID,
                TARGET_WEEKLY_QUANTITY_AMOUNT
        );

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.of(totalQuantity));

        totalQuantityCommandService.updateWeeklyQuantityAmount(command);

        assertThatTotalQuantity(totalQuantity).hasWeeklyAmounts(20, 50, 30);
        assertThatWeeklyQuantity(totalQuantity.getWeeklyQuantities().getFirst())
                .hasAmount(TARGET_WEEKLY_QUANTITY_AMOUNT);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
        then(totalQuantityRepository).should().save(totalQuantity);
    }

    @Test
    void 세션_총량이_없으면_주간_계획량을_줄일_수_없다() {
        WeeklyQuantityAmountUpdateCommand command = createWeeklyQuantityAmountUpdateCommand(
                FIRST_WEEKLY_QUANTITY_ID,
                TARGET_REDUCED_WEEKLY_QUANTITY_AMOUNT
        );

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.updateWeeklyQuantityAmount(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 주간_계획량_감소에_실패하면_저장하지_않는다() {
        TotalQuantity totalQuantity = createReconstructedTotalQuantityWithWeeklyAmounts(30, 40, 30);
        WeeklyQuantityAmountUpdateCommand command = createWeeklyQuantityAmountUpdateCommand(
                THIRD_WEEKLY_QUANTITY_ID,
                TARGET_REDUCED_WEEKLY_QUANTITY_AMOUNT
        );

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.of(totalQuantity));

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> totalQuantityCommandService.updateWeeklyQuantityAmount(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 주간_딜레이를_다른_주로_옮긴다() {
        TotalQuantity totalQuantity = createReconstructedTotalQuantityWithWeeklyAmounts(30, 40, 30);
        WeeklyAmountDelayCommand command = createWeeklyAmountDelayCommand(
                THIRD_WEEKLY_QUANTITY_ID,
                10
        );

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.of(totalQuantity));

        totalQuantityCommandService.delayWeeklyAmountTo(command);

        assertThatTotalQuantity(totalQuantity).hasWeeklyAmounts(20, 40, 40);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
        then(totalQuantityRepository).should().save(totalQuantity);
    }

    @Test
    void 세션_총량이_없으면_주간_딜레이를_옮길_수_없다() {
        WeeklyAmountDelayCommand command = createWeeklyAmountDelayCommandWithTotalQuantityId(
                UNKNOWN_TOTAL_QUANTITY_ID
        );

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                UNKNOWN_TOTAL_QUANTITY_ID
        )).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.delayWeeklyAmountTo(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                        SESSIONAL_PLAN_ID,
                        USER_ID,
                        UNKNOWN_TOTAL_QUANTITY_ID
                );
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 주간_딜레이_이동에_실패하면_저장하지_않는다() {
        TotalQuantity totalQuantity = createReconstructedTotalQuantityWithWeeklyAmounts(30, 70, 0);
        WeeklyAmountDelayCommand command = createWeeklyAmountDelayCommand(
                FIRST_WEEKLY_QUANTITY_ID,
                SECOND_WEEKLY_QUANTITY_ID,
                80
        );

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.of(totalQuantity));

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> totalQuantityCommandService.delayWeeklyAmountTo(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(WEEKLY_DELAY_AMOUNT_EXCEEDS_CURRENT_WEEKLY_DELAYED_AMOUNT);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 일일_딜레이를_같은_주의_다른_날로_옮긴다() {
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, user);
        TotalQuantity totalQuantity = createTotalQuantityWithDailyAmounts(
                FIRST_TOTAL_QUANTITY_ID,
                sessionalPlan,
                DailyQuantityAmount.of(40, 0, 40),
                DailyQuantityAmount.of(60, 0, 60)
        );
        DailyAmountDelayCommand command = createDailyAmountDelayCommand(10);

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.of(totalQuantity));

        totalQuantityCommandService.delayDailyAmountTo(command);

        WeeklyQuantity weeklyQuantity = totalQuantity.getWeeklyQuantities().getFirst();
        assertThatWeeklyQuantity(weeklyQuantity)
                .hasDailyAmounts(
                        DailyQuantityAmount.of(30, 0, 30),
                        DailyQuantityAmount.of(70, 0, 70)
                );

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
        then(totalQuantityRepository).should().save(totalQuantity);
    }

    @Test
    void 세션_총량이_없으면_일일_딜레이를_옮길_수_없다() {
        DailyAmountDelayCommand command = createDailyAmountDelayCommandWithTotalQuantityId(
                UNKNOWN_TOTAL_QUANTITY_ID
        );

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                UNKNOWN_TOTAL_QUANTITY_ID
        )).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.delayDailyAmountTo(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                        SESSIONAL_PLAN_ID,
                        USER_ID,
                        UNKNOWN_TOTAL_QUANTITY_ID
                );
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 일일_딜레이_이동에_실패하면_저장하지_않는다() {
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, user);
        TotalQuantity totalQuantity = createTotalQuantityWithDailyAmounts(
                FIRST_TOTAL_QUANTITY_ID,
                sessionalPlan,
                DailyQuantityAmount.of(40, 10, 30),
                DailyQuantityAmount.of(60, 0, 60)
        );
        DailyAmountDelayCommand command = createDailyAmountDelayCommand(40);

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.of(totalQuantity));

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> totalQuantityCommandService.delayDailyAmountTo(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_DELAY_AMOUNT_EXCEEDS_CURRENT_DAILY_DELAYED_AMOUNT);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 일일_계획량을_수정한다() {
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, user);
        TotalQuantity totalQuantity = createTotalQuantityWithDailyAmounts(
                FIRST_TOTAL_QUANTITY_ID,
                sessionalPlan,
                DailyQuantityAmount.of(40, 0, 40),
                DailyQuantityAmount.of(60, 0, 60)
        );
        DailyAmountUpdateCommand command = createDailyAmountUpdateCommand(20, TARGET_DAILY_MEMO);

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.of(totalQuantity));

        totalQuantityCommandService.updateDailyAmount(command);

        WeeklyQuantity weeklyQuantity = totalQuantity.getWeeklyQuantities().getFirst();
        assertThatWeeklyQuantity(weeklyQuantity)
                .hasDailyAmounts(
                        DailyQuantityAmount.of(20, 0, 20, TARGET_DAILY_MEMO),
                        DailyQuantityAmount.of(80, 0, 80)
                );

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
        then(totalQuantityRepository).should().save(totalQuantity);
    }

    @Test
    void 세션_총량이_없으면_일일_계획량을_수정할_수_없다() {
        DailyAmountUpdateCommand command = createDailyAmountUpdateCommand(20, TARGET_DAILY_MEMO);

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.updateDailyAmount(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 일일_계획량_수정에_실패하면_저장하지_않는다() {
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, user);
        TotalQuantity totalQuantity = createTotalQuantityWithDailyAmounts(
                FIRST_TOTAL_QUANTITY_ID,
                sessionalPlan,
                DailyQuantityAmount.of(40, 0, 40),
                DailyQuantityAmount.of(60, 0, 60)
        );
        DailyAmountUpdateCommand command = createDailyAmountUpdateCommand(50, TARGET_DAILY_MEMO);

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.of(totalQuantity));

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> totalQuantityCommandService.updateDailyAmount(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_AMOUNT_INCREASE_NOT_ALLOWED);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 일일_계획_달성량을_수정한다() {
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, user);
        TotalQuantity totalQuantity = createTotalQuantityWithDailyAmounts(
                FIRST_TOTAL_QUANTITY_ID,
                sessionalPlan,
                DailyQuantityAmount.of(40, 10, 30),
                DailyQuantityAmount.of(60, 0, 60)
        );
        DailyCompletedAmountUpdateCommand command = createDailyCompletedAmountUpdateCommand(30);

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.of(totalQuantity));

        totalQuantityCommandService.updateDailyCompletedAmount(command);

        WeeklyQuantity weeklyQuantity = totalQuantity.getWeeklyQuantities().getFirst();
        assertThatWeeklyQuantity(weeklyQuantity)
                .hasDailyAmounts(
                        DailyQuantityAmount.of(40, 30, 10),
                        DailyQuantityAmount.of(60, 0, 60)
                );

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
        then(totalQuantityRepository).should().save(totalQuantity);
    }

    @Test
    void 세션_총량이_없으면_일일_계획_달성량을_수정할_수_없다() {
        DailyCompletedAmountUpdateCommand command = createDailyCompletedAmountUpdateCommand(30);

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantityCommandService.updateDailyCompletedAmount(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(TOTAL_QUANTITY_NOT_FOUND);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
        then(totalQuantityRepository).should(never()).save(any());
    }

    @Test
    void 일일_계획_달성량_수정에_실패하면_저장하지_않는다() {
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, user);
        TotalQuantity totalQuantity = createTotalQuantityWithDailyAmounts(
                FIRST_TOTAL_QUANTITY_ID,
                sessionalPlan,
                DailyQuantityAmount.of(40, 10, 30),
                DailyQuantityAmount.of(60, 0, 60)
        );
        DailyCompletedAmountUpdateCommand command = createDailyCompletedAmountUpdateCommand(50);

        given(totalQuantityRepository.findBySessionalPlanIdAndUserIdAndTotalQuantityId(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        )).willReturn(Optional.of(totalQuantity));

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> totalQuantityCommandService.updateDailyCompletedAmount(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_AMOUNT_TO_COMPLETE_EXCEEDS_DELAYED_AMOUNT);

        then(totalQuantityRepository).should()
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID);
        then(totalQuantityRepository).should(never()).save(any());
    }
}
