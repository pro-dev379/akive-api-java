package net.akive.api.plan.adapter.webapi.totalquantity;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.totalquantity.dto.DailyAmountDelayCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.DailyAmountUpdateCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.DailyCompletedAmountUpdateCommand;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BadRequestError.DAILY_AMOUNT_INCREASE_NOT_ALLOWED;
import static net.akive.api.common.exception.business.BadRequestError.DAILY_DELAY_AMOUNT_EXCEEDS_CURRENT_DAILY_DELAYED_AMOUNT;
import static net.akive.api.common.exception.business.BadRequestError.DAILY_DELAY_RECEIVER_SAME_AS_SOURCE;
import static net.akive.api.common.exception.business.BadRequestError.LAST_DAILY_AMOUNT_REDUCTION_NOT_ALLOWED;
import static net.akive.api.common.exception.business.ConflictError.DAILY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT;
import static net.akive.api.common.exception.business.ConflictError.DAILY_AMOUNT_TO_COMPLETE_EXCEEDS_DELAYED_AMOUNT;
import static net.akive.api.common.exception.business.NotFoundError.*;
import static net.akive.api.plan.adapter.webapi.common.PlanApiPath.DAILY_COMPLETED_QUANTITIES;
import static net.akive.api.plan.adapter.webapi.common.PlanApiPath.DAILY_QUANTITY;
import static net.akive.api.plan.adapter.webapi.common.PlanApiPath.DAILY_QUANTITY_DELAYS;
import static net.akive.api.plan.domain.policy.PlanPolicy.QUANTITY_MEMO_MAX_LENGTH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

class DailyQuantityApiTest extends PrivateApiTestSupport {

    @Test
    void 일일_계획량을_수정한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        int targetDailyAmount = 50;
        String targetDailyMemo = "첫날 조정";
        String body = dailyAmountUpdateRequestJson(targetDailyAmount, targetDailyMemo);
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetDailyAmount,
                targetDailyMemo
        );

        patchJson(
                userId,
                DAILY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(NO_CONTENT);

        then(totalQuantityModifier).should().updateDailyAmount(command);
    }

    @ParameterizedTest
    @MethodSource("invalidDailyAmountUpdateRequests")
    void 일일_계획량_수정_요청값이_올바르지_않으면_400을_응답한다(String body) {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;

        patchJson(
                userId,
                DAILY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(BAD_REQUEST);

        then(totalQuantityModifier).should(never()).updateDailyAmount(any(DailyAmountUpdateCommand.class));
    }

    @Test
    void 일일_계획량_수정_시_세셔널_플랜이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        int targetDailyAmount = 50;
        String body = dailyAmountUpdateRequestJson(targetDailyAmount, null);
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetDailyAmount,
                null
        );
        willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateDailyAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(NOT_FOUND);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_SESSIONAL_PLAN_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Sessional plan not found.");
        then(totalQuantityModifier).should().updateDailyAmount(command);
    }

    @Test
    void 일일_계획량_수정_시_세션_총량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        int targetDailyAmount = 50;
        String body = dailyAmountUpdateRequestJson(targetDailyAmount, null);
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetDailyAmount,
                null
        );
        willThrow(new NotFoundException(TOTAL_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateDailyAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(NOT_FOUND);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_TOTAL_QUANTITY_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Total quantity not found.");
        then(totalQuantityModifier).should().updateDailyAmount(command);
    }

    @Test
    void 일일_계획량_수정_시_주간_계획량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        int targetDailyAmount = 50;
        String body = dailyAmountUpdateRequestJson(targetDailyAmount, null);
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetDailyAmount,
                null
        );
        willThrow(new NotFoundException(WEEKLY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateDailyAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(NOT_FOUND);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_WEEKLY_QUANTITY_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Weekly quantity not found.");
        then(totalQuantityModifier).should().updateDailyAmount(command);
    }

    @Test
    void 일일_계획량_수정_시_일일_계획량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        int targetDailyAmount = 50;
        String body = dailyAmountUpdateRequestJson(targetDailyAmount, null);
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetDailyAmount,
                null
        );
        willThrow(new NotFoundException(DAILY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateDailyAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(NOT_FOUND);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_DAILY_QUANTITY_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Daily quantity not found");
        then(totalQuantityModifier).should().updateDailyAmount(command);
    }

    @Test
    void 일일_계획량_수정_시_일일_계획량을_늘리면_400을_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        int targetDailyAmount = 50;
        String body = dailyAmountUpdateRequestJson(targetDailyAmount, null);
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetDailyAmount,
                null
        );
        willThrow(new BadRequestException(DAILY_AMOUNT_INCREASE_NOT_ALLOWED))
                .given(totalQuantityModifier)
                .updateDailyAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(BAD_REQUEST);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_400_DAILY_AMOUNT_INCREASE_NOT_ALLOWED");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Daily amount cannot be increased here.");
        then(totalQuantityModifier).should().updateDailyAmount(command);
    }

    @Test
    void 일일_계획량_수정_시_마지막_일일_계획량을_줄이면_400을_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        int targetDailyAmount = 50;
        String body = dailyAmountUpdateRequestJson(targetDailyAmount, null);
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetDailyAmount,
                null
        );
        willThrow(new BadRequestException(LAST_DAILY_AMOUNT_REDUCTION_NOT_ALLOWED))
                .given(totalQuantityModifier)
                .updateDailyAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(BAD_REQUEST);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_400_LAST_DAILY_AMOUNT_REDUCTION_NOT_ALLOWED");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Last daily amount cannot be reduced.");
        then(totalQuantityModifier).should().updateDailyAmount(command);
    }

    @Test
    void 일일_계획량_수정_시_일일_계획_달성량보다_적게_수정하면_409를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        int targetDailyAmount = 50;
        String body = dailyAmountUpdateRequestJson(targetDailyAmount, null);
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetDailyAmount,
                null
        );
        willThrow(new ConflictException(DAILY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT))
                .given(totalQuantityModifier)
                .updateDailyAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(CONFLICT);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_409_DAILY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Daily amount cannot be less than completed amount.");
        then(totalQuantityModifier).should().updateDailyAmount(command);
    }

    @Test
    void 일일_딜레이를_같은_주의_다른_날로_옮긴다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        Long receivingDailyQuantityId = 50L;
        int dailyAmountToDelay = 10;
        String body = dailyAmountDelayRequestJson(receivingDailyQuantityId, dailyAmountToDelay);
        DailyAmountDelayCommand command = DailyAmountDelayCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingDailyQuantityId,
                dailyAmountToDelay
        );

        patchJson(
                userId,
                DAILY_QUANTITY_DELAYS,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(NO_CONTENT);

        then(totalQuantityModifier).should().delayDailyAmountTo(command);
    }

    @ParameterizedTest
    @MethodSource("invalidDailyAmountDelayRequests")
    void 일일_딜레이_옮길_시_요청값이_올바르지_않으면_400을_응답한다(String body) {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;

        patchJson(
                userId,
                DAILY_QUANTITY_DELAYS,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(BAD_REQUEST);

        then(totalQuantityModifier).should(never()).delayDailyAmountTo(any(DailyAmountDelayCommand.class));
    }

    @Test
    void 일일_딜레이_옮길_시_세셔널_플랜이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        Long receivingDailyQuantityId = 50L;
        int dailyAmountToDelay = 10;
        String body = dailyAmountDelayRequestJson(receivingDailyQuantityId, dailyAmountToDelay);
        DailyAmountDelayCommand command = DailyAmountDelayCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingDailyQuantityId,
                dailyAmountToDelay
        );
        willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(totalQuantityModifier)
                .delayDailyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_QUANTITY_DELAYS,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(NOT_FOUND);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_SESSIONAL_PLAN_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Sessional plan not found.");
        then(totalQuantityModifier).should().delayDailyAmountTo(command);
    }

    @Test
    void 일일_딜레이_옮길_시_세션_총량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        Long receivingDailyQuantityId = 50L;
        int dailyAmountToDelay = 10;
        String body = dailyAmountDelayRequestJson(receivingDailyQuantityId, dailyAmountToDelay);
        DailyAmountDelayCommand command = DailyAmountDelayCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingDailyQuantityId,
                dailyAmountToDelay
        );
        willThrow(new NotFoundException(TOTAL_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .delayDailyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_QUANTITY_DELAYS,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(NOT_FOUND);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_TOTAL_QUANTITY_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Total quantity not found.");
        then(totalQuantityModifier).should().delayDailyAmountTo(command);
    }

    @Test
    void 일일_딜레이_옮길_시_주간_계획량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        Long receivingDailyQuantityId = 50L;
        int dailyAmountToDelay = 10;
        String body = dailyAmountDelayRequestJson(receivingDailyQuantityId, dailyAmountToDelay);
        DailyAmountDelayCommand command = DailyAmountDelayCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingDailyQuantityId,
                dailyAmountToDelay
        );
        willThrow(new NotFoundException(WEEKLY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .delayDailyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_QUANTITY_DELAYS,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(NOT_FOUND);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_WEEKLY_QUANTITY_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Weekly quantity not found.");
        then(totalQuantityModifier).should().delayDailyAmountTo(command);
    }

    @Test
    void 일일_딜레이_옮길_시_일일_계획량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        Long receivingDailyQuantityId = 50L;
        int dailyAmountToDelay = 10;
        String body = dailyAmountDelayRequestJson(receivingDailyQuantityId, dailyAmountToDelay);
        DailyAmountDelayCommand command = DailyAmountDelayCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingDailyQuantityId,
                dailyAmountToDelay
        );
        willThrow(new NotFoundException(DAILY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .delayDailyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_QUANTITY_DELAYS,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(NOT_FOUND);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_DAILY_QUANTITY_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Daily quantity not found");
        then(totalQuantityModifier).should().delayDailyAmountTo(command);
    }

    @Test
    void 일일_딜레이_옮길_시_같은_날로_옮기면_400을_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        Long receivingDailyQuantityId = 50L;
        int dailyAmountToDelay = 10;
        String body = dailyAmountDelayRequestJson(receivingDailyQuantityId, dailyAmountToDelay);
        DailyAmountDelayCommand command = DailyAmountDelayCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingDailyQuantityId,
                dailyAmountToDelay
        );
        willThrow(new BadRequestException(DAILY_DELAY_RECEIVER_SAME_AS_SOURCE))
                .given(totalQuantityModifier)
                .delayDailyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_QUANTITY_DELAYS,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(BAD_REQUEST);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_400_DAILY_DELAY_RECEIVER_SAME_AS_SOURCE");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Daily quantity receiving the delay must be different from source daily quantity.");
        then(totalQuantityModifier).should().delayDailyAmountTo(command);
    }

    @Test
    void 일일_딜레이_옮길_시_현재_일일_딜레이보다_많은_수량을_옮기면_400을_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        Long receivingDailyQuantityId = 50L;
        int dailyAmountToDelay = 10;
        String body = dailyAmountDelayRequestJson(receivingDailyQuantityId, dailyAmountToDelay);
        DailyAmountDelayCommand command = DailyAmountDelayCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingDailyQuantityId,
                dailyAmountToDelay
        );
        willThrow(new BadRequestException(DAILY_DELAY_AMOUNT_EXCEEDS_CURRENT_DAILY_DELAYED_AMOUNT))
                .given(totalQuantityModifier)
                .delayDailyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_QUANTITY_DELAYS,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(BAD_REQUEST);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_400_DAILY_DELAY_AMOUNT_EXCEEDS_CURRENT_DAILY_DELAYED_AMOUNT");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Daily delay amount exceeds current daily delayed amount.");
        then(totalQuantityModifier).should().delayDailyAmountTo(command);
    }

    @Test
    void 일일_계획_달성량을_수정한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        int targetDailyCompletedAmount = 50;
        String body = dailyCompletedAmountUpdateRequestJson(targetDailyCompletedAmount);
        DailyCompletedAmountUpdateCommand command = DailyCompletedAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetDailyCompletedAmount
        );

        patchJson(
                userId,
                DAILY_COMPLETED_QUANTITIES,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(NO_CONTENT);

        then(totalQuantityModifier).should().updateDailyCompletedAmount(command);
    }

    @ParameterizedTest
    @MethodSource("invalidDailyCompletedAmountUpdateRequests")
    void 일일_계획_달성량_수정_요청값이_올바르지_않으면_400을_응답한다(String body) {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;

        patchJson(
                userId,
                DAILY_COMPLETED_QUANTITIES,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(BAD_REQUEST);

        then(totalQuantityModifier).should(never())
                .updateDailyCompletedAmount(any(DailyCompletedAmountUpdateCommand.class));
    }

    @Test
    void 일일_계획_달성량_수정_시_세셔널_플랜이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        int targetDailyCompletedAmount = 50;
        String body = dailyCompletedAmountUpdateRequestJson(targetDailyCompletedAmount);
        DailyCompletedAmountUpdateCommand command = DailyCompletedAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetDailyCompletedAmount
        );
        willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateDailyCompletedAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_COMPLETED_QUANTITIES,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(NOT_FOUND);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_SESSIONAL_PLAN_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Sessional plan not found.");
        then(totalQuantityModifier).should().updateDailyCompletedAmount(command);
    }

    @Test
    void 일일_계획_달성량_수정_시_세션_총량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        int targetDailyCompletedAmount = 50;
        String body = dailyCompletedAmountUpdateRequestJson(targetDailyCompletedAmount);
        DailyCompletedAmountUpdateCommand command = DailyCompletedAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetDailyCompletedAmount
        );
        willThrow(new NotFoundException(TOTAL_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateDailyCompletedAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_COMPLETED_QUANTITIES,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(NOT_FOUND);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_TOTAL_QUANTITY_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Total quantity not found.");
        then(totalQuantityModifier).should().updateDailyCompletedAmount(command);
    }

    @Test
    void 일일_계획_달성량_수정_시_주간_계획량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        int targetDailyCompletedAmount = 50;
        String body = dailyCompletedAmountUpdateRequestJson(targetDailyCompletedAmount);
        DailyCompletedAmountUpdateCommand command = DailyCompletedAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetDailyCompletedAmount
        );
        willThrow(new NotFoundException(WEEKLY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateDailyCompletedAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_COMPLETED_QUANTITIES,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(NOT_FOUND);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_WEEKLY_QUANTITY_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Weekly quantity not found.");
        then(totalQuantityModifier).should().updateDailyCompletedAmount(command);
    }

    @Test
    void 일일_계획_달성량_수정_시_일일_계획량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        int targetDailyCompletedAmount = 50;
        String body = dailyCompletedAmountUpdateRequestJson(targetDailyCompletedAmount);
        DailyCompletedAmountUpdateCommand command = DailyCompletedAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetDailyCompletedAmount
        );
        willThrow(new NotFoundException(DAILY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateDailyCompletedAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_COMPLETED_QUANTITIES,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(NOT_FOUND);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_DAILY_QUANTITY_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Daily quantity not found");
        then(totalQuantityModifier).should().updateDailyCompletedAmount(command);
    }

    @Test
    void 일일_계획_달성량_수정_시_달성량_증가분이_딜레이량보다_많으면_409를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long dailyQuantityId = 40L;
        int targetDailyCompletedAmount = 50;
        String body = dailyCompletedAmountUpdateRequestJson(targetDailyCompletedAmount);
        DailyCompletedAmountUpdateCommand command = DailyCompletedAmountUpdateCommand.of(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetDailyCompletedAmount
        );
        willThrow(new ConflictException(DAILY_AMOUNT_TO_COMPLETE_EXCEEDS_DELAYED_AMOUNT))
                .given(totalQuantityModifier)
                .updateDailyCompletedAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                DAILY_COMPLETED_QUANTITIES,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId,
                dailyQuantityId
        ).hasStatus(CONFLICT);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_409_DAILY_AMOUNT_TO_COMPLETE_EXCEEDS_DELAYED_AMOUNT");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Daily amount to complete cannot exceed delayed amount.");
        then(totalQuantityModifier).should().updateDailyCompletedAmount(command);
    }

    private static Stream<String> invalidDailyAmountUpdateRequests() {
        return Stream.of(
                """
                {
                  "targetDailyAmount": null
                }
                """,
                dailyAmountUpdateRequestJson(null, null),
                dailyAmountUpdateRequestJson(-1, null),
                dailyAmountUpdateRequestJson(50, "a".repeat(QUANTITY_MEMO_MAX_LENGTH + 1))
        );
    }

    private static Stream<String> invalidDailyCompletedAmountUpdateRequests() {
        return Stream.of(
                """
                {
                  "targetDailyCompletedAmount": null
                }
                """,
                dailyCompletedAmountUpdateRequestJson(null),
                dailyCompletedAmountUpdateRequestJson(-1)
        );
    }

    private static Stream<String> invalidDailyAmountDelayRequests() {
        return Stream.of(
                """
                {
                  "receivingDailyQuantityId": null,
                  "dailyAmountToDelay": 10
                }
                """,
                dailyAmountDelayRequestJson(-1L, 10),
                dailyAmountDelayRequestJson(50L, null),
                dailyAmountDelayRequestJson(50L, -1)
        );
    }

    private static String dailyAmountUpdateRequestJson(Integer targetDailyAmount, String targetDailyMemo) {
        return """
                {
                  "targetDailyAmount": {
                    "quantityAmount": %s,
                    "quantityMemo": %s
                  }
                }
                """.formatted(
                targetDailyAmount,
                targetDailyMemo == null ? null : "\"" + targetDailyMemo + "\""
        );
    }

    private static String dailyCompletedAmountUpdateRequestJson(Integer targetDailyCompletedAmount) {
        return """
                {
                  "targetDailyCompletedAmount": %s
                }
                """.formatted(targetDailyCompletedAmount);
    }

    private static String dailyAmountDelayRequestJson(Long receivingDailyQuantityId, Integer dailyAmountToDelay) {
        return """
                {
                  "receivingDailyQuantityId": %s,
                  "dailyAmountToDelay": %s
                }
                """.formatted(receivingDailyQuantityId, dailyAmountToDelay);
    }
}
