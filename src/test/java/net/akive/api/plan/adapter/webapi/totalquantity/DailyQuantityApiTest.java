package net.akive.api.plan.adapter.webapi.totalquantity;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.adapter.webapi.common.PlanApiPath;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityModifier;
import net.akive.api.plan.application.provided.totalquantity.dto.DailyAmountDelayCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.DailyAmountUpdateCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.DailyCompletedAmountUpdateCommand;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.common.exception.business.ResourceNotFoundError.*;
import static net.akive.api.plan.domain.fixture.DailyQuantityFixture.*;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.SESSIONAL_PLAN_ID;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.FIRST_TOTAL_QUANTITY_ID;
import static net.akive.api.plan.domain.fixture.WeeklyQuantityFixture.FIRST_WEEKLY_QUANTITY_ID;
import static net.akive.api.plan.domain.policy.PlanPolicy.QUANTITY_MEMO_MAX_LENGTH;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(DailyQuantityApi.class)
class DailyQuantityApiTest extends PrivateApiTestSupport {

    @MockitoBean
    TotalQuantityModifier totalQuantityModifier;

    @Test
    void 일일_계획량을_수정한다() {
        String body = dailyAmountUpdateRequestJson(TARGET_DAILY_AMOUNT, TARGET_DAILY_MEMO);
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TARGET_DAILY_AMOUNT,
                TARGET_DAILY_MEMO
        );

        patchJson(
                PlanApiPath.DAILY_QUANTITY,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(NO_CONTENT);

        then(totalQuantityModifier).should().updateDailyAmount(command);
    }

    @ParameterizedTest
    @MethodSource("invalidDailyAmountUpdateRequests")
    void 일일_계획량_수정_요청값이_올바르지_않으면_400을_응답한다(String body) {

        patchJson(
                PlanApiPath.DAILY_QUANTITY,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        then(totalQuantityModifier).should(never()).updateDailyAmount(any(DailyAmountUpdateCommand.class));
    }


    @Test
    void 일일_계획량_수정_시_세션_총량이_없으면_404를_응답한다() {
        String body = dailyAmountUpdateRequestJson(TARGET_DAILY_AMOUNT, null);
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TARGET_DAILY_AMOUNT,
                null
        );
        willThrow(new ResourceNotFoundException(TOTAL_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateDailyAmount(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.DAILY_QUANTITY,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, TOTAL_QUANTITY_NOT_FOUND);
        then(totalQuantityModifier).should().updateDailyAmount(command);
    }

    @Test
    void 일일_계획량_수정_시_주간_계획량이_없으면_404를_응답한다() {
        String body = dailyAmountUpdateRequestJson(TARGET_DAILY_AMOUNT, null);
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TARGET_DAILY_AMOUNT,
                null
        );
        willThrow(new ResourceNotFoundException(WEEKLY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateDailyAmount(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.DAILY_QUANTITY,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, WEEKLY_QUANTITY_NOT_FOUND);
        then(totalQuantityModifier).should().updateDailyAmount(command);
    }

    @Test
    void 일일_계획량_수정_시_일일_계획량이_없으면_404를_응답한다() {
        String body = dailyAmountUpdateRequestJson(TARGET_DAILY_AMOUNT, null);
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TARGET_DAILY_AMOUNT,
                null
        );
        willThrow(new ResourceNotFoundException(DAILY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateDailyAmount(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.DAILY_QUANTITY,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, DAILY_QUANTITY_NOT_FOUND);
        then(totalQuantityModifier).should().updateDailyAmount(command);
    }

    @Test
    void 일일_계획량_수정_시_일일_계획량을_늘리면_400을_응답한다() {
        String body = dailyAmountUpdateRequestJson(TARGET_DAILY_AMOUNT, null);
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TARGET_DAILY_AMOUNT,
                null
        );
        willThrow(new BusinessRuleViolationException(DAILY_AMOUNT_INCREASE_NOT_ALLOWED))
                .given(totalQuantityModifier)
                .updateDailyAmount(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.DAILY_QUANTITY,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_AMOUNT_INCREASE_NOT_ALLOWED);
        then(totalQuantityModifier).should().updateDailyAmount(command);
    }

    @Test
    void 일일_계획량_수정_시_마지막_일일_계획량을_줄이면_400을_응답한다() {
        String body = dailyAmountUpdateRequestJson(TARGET_DAILY_AMOUNT, null);
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TARGET_DAILY_AMOUNT,
                null
        );
        willThrow(new BusinessRuleViolationException(LAST_DAILY_AMOUNT_REDUCTION_NOT_ALLOWED))
                .given(totalQuantityModifier)
                .updateDailyAmount(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.DAILY_QUANTITY,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, LAST_DAILY_AMOUNT_REDUCTION_NOT_ALLOWED);
        then(totalQuantityModifier).should().updateDailyAmount(command);
    }

    @Test
    void 일일_계획량_수정_시_일일_계획_달성량보다_적게_수정하면_400을_응답한다() {
        String body = dailyAmountUpdateRequestJson(TARGET_DAILY_AMOUNT, null);
        DailyAmountUpdateCommand command = DailyAmountUpdateCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TARGET_DAILY_AMOUNT,
                null
        );
        willThrow(new BusinessRuleViolationException(DAILY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT))
                .given(totalQuantityModifier)
                .updateDailyAmount(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.DAILY_QUANTITY,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT);
        then(totalQuantityModifier).should().updateDailyAmount(command);
    }

    @Test
    void 일일_딜레이를_같은_주의_다른_날로_옮긴다() {
        String body = dailyAmountDelayRequestJson(SECOND_DAILY_QUANTITY_ID, DAILY_AMOUNT_TO_DELAY);
        DailyAmountDelayCommand command = DailyAmountDelayCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                SECOND_DAILY_QUANTITY_ID,
                DAILY_AMOUNT_TO_DELAY
        );

        patchJson(
                PlanApiPath.DAILY_QUANTITY_DELAYS,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(NO_CONTENT);

        then(totalQuantityModifier).should().delayDailyAmountTo(command);
    }

    @ParameterizedTest
    @MethodSource("invalidDailyAmountDelayRequests")
    void 일일_딜레이_옮길_시_요청값이_올바르지_않으면_400을_응답한다(String body) {

        patchJson(
                PlanApiPath.DAILY_QUANTITY_DELAYS,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        then(totalQuantityModifier).should(never()).delayDailyAmountTo(any(DailyAmountDelayCommand.class));
    }


    @Test
    void 일일_딜레이_옮길_시_세션_총량이_없으면_404를_응답한다() {
        String body = dailyAmountDelayRequestJson(SECOND_DAILY_QUANTITY_ID, DAILY_AMOUNT_TO_DELAY);
        DailyAmountDelayCommand command = DailyAmountDelayCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                SECOND_DAILY_QUANTITY_ID,
                DAILY_AMOUNT_TO_DELAY
        );
        willThrow(new ResourceNotFoundException(TOTAL_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .delayDailyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.DAILY_QUANTITY_DELAYS,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, TOTAL_QUANTITY_NOT_FOUND);
        then(totalQuantityModifier).should().delayDailyAmountTo(command);
    }

    @Test
    void 일일_딜레이_옮길_시_주간_계획량이_없으면_404를_응답한다() {
        String body = dailyAmountDelayRequestJson(SECOND_DAILY_QUANTITY_ID, DAILY_AMOUNT_TO_DELAY);
        DailyAmountDelayCommand command = DailyAmountDelayCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                SECOND_DAILY_QUANTITY_ID,
                DAILY_AMOUNT_TO_DELAY
        );
        willThrow(new ResourceNotFoundException(WEEKLY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .delayDailyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.DAILY_QUANTITY_DELAYS,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, WEEKLY_QUANTITY_NOT_FOUND);
        then(totalQuantityModifier).should().delayDailyAmountTo(command);
    }

    @Test
    void 일일_딜레이_옮길_시_일일_계획량이_없으면_404를_응답한다() {
        String body = dailyAmountDelayRequestJson(SECOND_DAILY_QUANTITY_ID, DAILY_AMOUNT_TO_DELAY);
        DailyAmountDelayCommand command = DailyAmountDelayCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                SECOND_DAILY_QUANTITY_ID,
                DAILY_AMOUNT_TO_DELAY
        );
        willThrow(new ResourceNotFoundException(DAILY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .delayDailyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.DAILY_QUANTITY_DELAYS,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, DAILY_QUANTITY_NOT_FOUND);
        then(totalQuantityModifier).should().delayDailyAmountTo(command);
    }

    @Test
    void 일일_딜레이_옮길_시_같은_날로_옮기면_400을_응답한다() {
        String body = dailyAmountDelayRequestJson(FIRST_DAILY_QUANTITY_ID, DAILY_AMOUNT_TO_DELAY);
        DailyAmountDelayCommand command = DailyAmountDelayCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_DAILY_QUANTITY_ID,
                DAILY_AMOUNT_TO_DELAY
        );
        willThrow(new BusinessRuleViolationException(DAILY_DELAY_RECEIVER_SAME_AS_SOURCE))
                .given(totalQuantityModifier)
                .delayDailyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.DAILY_QUANTITY_DELAYS,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_DELAY_RECEIVER_SAME_AS_SOURCE);
        then(totalQuantityModifier).should().delayDailyAmountTo(command);
    }

    @Test
    void 일일_딜레이_옮길_시_현재_일일_딜레이보다_많은_수량을_옮기면_400을_응답한다() {
        String body = dailyAmountDelayRequestJson(SECOND_DAILY_QUANTITY_ID, DAILY_AMOUNT_TO_DELAY);
        DailyAmountDelayCommand command = DailyAmountDelayCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                SECOND_DAILY_QUANTITY_ID,
                DAILY_AMOUNT_TO_DELAY
        );
        willThrow(new BusinessRuleViolationException(DAILY_DELAY_AMOUNT_EXCEEDS_CURRENT_DAILY_DELAYED_AMOUNT))
                .given(totalQuantityModifier)
                .delayDailyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.DAILY_QUANTITY_DELAYS,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_DELAY_AMOUNT_EXCEEDS_CURRENT_DAILY_DELAYED_AMOUNT);
        then(totalQuantityModifier).should().delayDailyAmountTo(command);
    }

    @Test
    void 일일_계획_달성량을_수정한다() {
        String body = dailyCompletedAmountUpdateRequestJson(TARGET_DAILY_COMPLETED_AMOUNT);
        DailyCompletedAmountUpdateCommand command = DailyCompletedAmountUpdateCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TARGET_DAILY_COMPLETED_AMOUNT
        );

        patchJson(
                PlanApiPath.DAILY_COMPLETED_QUANTITIES,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(NO_CONTENT);

        then(totalQuantityModifier).should().updateDailyCompletedAmount(command);
    }

    @ParameterizedTest
    @MethodSource("invalidDailyCompletedAmountUpdateRequests")
    void 일일_계획_달성량_수정_요청값이_올바르지_않으면_400을_응답한다(String body) {

        patchJson(
                PlanApiPath.DAILY_COMPLETED_QUANTITIES,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        then(totalQuantityModifier).should(never())
                .updateDailyCompletedAmount(any(DailyCompletedAmountUpdateCommand.class));
    }

    @Test
    void 일일_계획_달성량_수정_시_달성량_증가분이_딜레이량보다_많으면_400을_응답한다() {
        String body = dailyCompletedAmountUpdateRequestJson(TARGET_DAILY_COMPLETED_AMOUNT);
        DailyCompletedAmountUpdateCommand command = DailyCompletedAmountUpdateCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TARGET_DAILY_COMPLETED_AMOUNT
        );
        willThrow(new BusinessRuleViolationException(DAILY_AMOUNT_TO_COMPLETE_EXCEEDS_DELAYED_AMOUNT))
                .given(totalQuantityModifier)
                .updateDailyCompletedAmount(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.DAILY_COMPLETED_QUANTITIES,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_AMOUNT_TO_COMPLETE_EXCEEDS_DELAYED_AMOUNT);
        then(totalQuantityModifier).should().updateDailyCompletedAmount(command);
    }


    @Test
    void 일일_계획_달성량_수정_시_세션_총량이_없으면_404를_응답한다() {
        String body = dailyCompletedAmountUpdateRequestJson(TARGET_DAILY_COMPLETED_AMOUNT);
        DailyCompletedAmountUpdateCommand command = DailyCompletedAmountUpdateCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TARGET_DAILY_COMPLETED_AMOUNT
        );
        willThrow(new ResourceNotFoundException(TOTAL_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateDailyCompletedAmount(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.DAILY_COMPLETED_QUANTITIES,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, TOTAL_QUANTITY_NOT_FOUND);
        then(totalQuantityModifier).should().updateDailyCompletedAmount(command);
    }

    @Test
    void 일일_계획_달성량_수정_시_주간_계획량이_없으면_404를_응답한다() {
        String body = dailyCompletedAmountUpdateRequestJson(TARGET_DAILY_COMPLETED_AMOUNT);
        DailyCompletedAmountUpdateCommand command = DailyCompletedAmountUpdateCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TARGET_DAILY_COMPLETED_AMOUNT
        );
        willThrow(new ResourceNotFoundException(WEEKLY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateDailyCompletedAmount(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.DAILY_COMPLETED_QUANTITIES,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, WEEKLY_QUANTITY_NOT_FOUND);
        then(totalQuantityModifier).should().updateDailyCompletedAmount(command);
    }

    @Test
    void 일일_계획_달성량_수정_시_일일_계획량이_없으면_404를_응답한다() {
        String body = dailyCompletedAmountUpdateRequestJson(TARGET_DAILY_COMPLETED_AMOUNT);
        DailyCompletedAmountUpdateCommand command = DailyCompletedAmountUpdateCommand.of(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TARGET_DAILY_COMPLETED_AMOUNT
        );
        willThrow(new ResourceNotFoundException(DAILY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateDailyCompletedAmount(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.DAILY_COMPLETED_QUANTITIES,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, DAILY_QUANTITY_NOT_FOUND);
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
