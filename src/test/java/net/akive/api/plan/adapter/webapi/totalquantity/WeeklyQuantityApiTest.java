package net.akive.api.plan.adapter.webapi.totalquantity;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyAmountDelayCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyPlanItemFindQuery;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyQuantityAmountUpdateCommand;
import net.akive.api.plan.application.result.WeeklyPlan;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.model.WeeklyQuantity;
import net.akive.api.support.PrivateApiTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BadRequestError.*;
import static net.akive.api.common.exception.business.NotFoundError.*;
import static net.akive.api.plan.adapter.webapi.common.PlanApiPath.WEEKLY_QUANTITY;
import static net.akive.api.plan.adapter.webapi.common.PlanApiPath.WEEKLY_QUANTITY_DELAYS;
import static net.akive.api.plan.domain.PlanFixture.*;
import static net.akive.api.plan.domain.policy.PlanPolicy.QUANTITY_MEMO_MAX_LENGTH;
import static net.akive.api.user.domain.UserFixture.createPersistedUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

class WeeklyQuantityApiTest extends PrivateApiTestSupport {

    @Test
    void 주간_계획량을_조회한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 1L;
        WeeklyPlan.WeeklyPlanItem weeklyPlanItem =
                createWeeklyPlanItem(userId, sessionalPlanId, totalQuantityId, weeklyQuantityId);
        WeeklyPlanItemFindQuery query = new WeeklyPlanItemFindQuery(
                sessionalPlanId,
                userId,
                totalQuantityId,
                weeklyQuantityId
        );
        given(weeklyQuantityFinder.findWeeklyPlanItem(query)).willReturn(weeklyPlanItem);

        MvcTestResultAssert result = get(
                userId,
                WEEKLY_QUANTITY,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatusOk();

        result.bodyJson().extractingPath("$.sessionalPlanId").isEqualTo(10);
        result.bodyJson().extractingPath("$.totalQuantityId").isEqualTo(20);
        result.bodyJson().extractingPath("$.weeklyQuantityId").isEqualTo(1);
        result.bodyJson().extractingPath("$.subject.name").isEqualTo(SUBJECT_NAME);
        result.bodyJson().extractingPath("$.subject.category").isEqualTo(SUBJECT_CATEGORY);
        result.bodyJson().extractingPath("$.weeklyQuantityAmount.quantityAmount").isEqualTo(TOTAL_AMOUNT);
        result.bodyJson().extractingPath("$.weeklyQuantityAmount.quantityUnit").isEqualTo(QUANTITY_UNIT);
        result.bodyJson().extractingPath("$.weeklyQuantityAmount.quantityMemo").isNull();
        result.bodyJson().extractingPath("$.dailyQuantities[0].dailyQuantityId").isEqualTo(1);
        result.bodyJson().extractingPath("$.dailyQuantities[0].planDate.date").isEqualTo("2026-01-01");
        result.bodyJson()
                .extractingPath("$.dailyQuantities[0].dailyQuantityAmount.quantityAmount")
                .isEqualTo(100);
        result.bodyJson()
                .extractingPath("$.dailyQuantities[0].dailyQuantityAmount.completedQuantityAmount")
                .isEqualTo(30);
        result.bodyJson()
                .extractingPath("$.dailyQuantities[0].dailyQuantityAmount.delayedQuantityAmount")
                .isEqualTo(70);

        then(weeklyQuantityFinder).should().findWeeklyPlanItem(query);
    }

    @Test
    void 주간_계획량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 999L;
        WeeklyPlanItemFindQuery query = new WeeklyPlanItemFindQuery(
                sessionalPlanId,
                userId,
                totalQuantityId,
                weeklyQuantityId
        );
        given(weeklyQuantityFinder.findWeeklyPlanItem(query))
                .willThrow(new NotFoundException(WEEKLY_PLAN_ITEM_NOT_FOUND));

        MvcTestResultAssert result = get(
                userId,
                WEEKLY_QUANTITY,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(NOT_FOUND);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_WEEKLY_PLAN_ITEM_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Weekly plan item not found.");

        then(weeklyQuantityFinder).should().findWeeklyPlanItem(query);
    }

    @Test
    void 주간_계획량을_줄인다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityAmountUpdateCommand command = WeeklyQuantityAmountUpdateCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                TOTAL_AMOUNT,
                null
        );

        patchJson(
                userId,
                WEEKLY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(NO_CONTENT);

        then(totalQuantityModifier).should().updateWeeklyQuantityAmount(command);
    }

    @ParameterizedTest
    @MethodSource("invalidWeeklyQuantityReduceRequests")
    void 주간_계획량_수정_요청값이_올바르지_않으면_400을_응답한다(String body) {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;

        patchJson(
                userId,
                WEEKLY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(BAD_REQUEST);

        then(totalQuantityModifier)
                .should(never())
                .updateWeeklyQuantityAmount(any(WeeklyQuantityAmountUpdateCommand.class));
    }

    @Test
    void 주간_계획량_수정_시_세셔널_플랜이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityAmountUpdateCommand command = WeeklyQuantityAmountUpdateCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateWeeklyQuantityAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(NOT_FOUND);

        assertSessionalPlanNotFoundResponse(result);

        then(totalQuantityModifier).should().updateWeeklyQuantityAmount(command);
    }

    @Test
    void 주간_계획량_수정_시_세션_총량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityAmountUpdateCommand command = WeeklyQuantityAmountUpdateCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new NotFoundException(TOTAL_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateWeeklyQuantityAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(NOT_FOUND);

        assertTotalQuantityNotFoundResponse(result);

        then(totalQuantityModifier).should().updateWeeklyQuantityAmount(command);
    }

    @Test
    void 주간_계획량_수정_시_주간_계획량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityAmountUpdateCommand command = WeeklyQuantityAmountUpdateCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new NotFoundException(WEEKLY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateWeeklyQuantityAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(NOT_FOUND);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_WEEKLY_QUANTITY_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Weekly quantity not found.");

        then(totalQuantityModifier).should().updateWeeklyQuantityAmount(command);
    }

    @Test
    void 주간_계획량_수정_시_마지막_주간_계획량을_줄이면_400을_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityAmountUpdateCommand command = WeeklyQuantityAmountUpdateCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new BadRequestException(LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED))
                .given(totalQuantityModifier)
                .updateWeeklyQuantityAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(BAD_REQUEST);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_400_LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Last weekly quantity cannot be reduced.");

        then(totalQuantityModifier).should().updateWeeklyQuantityAmount(command);
    }

    @Test
    void 주간_계획량_수정_시_주간_계획량을_늘리면_400을_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityAmountUpdateCommand command = WeeklyQuantityAmountUpdateCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new BadRequestException(WEEKLY_AMOUNT_INCREASE_NOT_ALLOWED))
                .given(totalQuantityModifier)
                .updateWeeklyQuantityAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(BAD_REQUEST);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_400_WEEKLY_AMOUNT_INCREASE_NOT_ALLOWED");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Weekly amount cannot be increased here.");

        then(totalQuantityModifier).should().updateWeeklyQuantityAmount(command);
    }

    @Test
    void 주간_계획량_수정_시_주간_계획_달성량보다_적게_수정하면_400을_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityAmountUpdateCommand command = WeeklyQuantityAmountUpdateCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new BadRequestException(WEEKLY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT))
                .given(totalQuantityModifier)
                .updateWeeklyQuantityAmount(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(BAD_REQUEST);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_400_WEEKLY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Weekly amount cannot be less than completed amount.");

        then(totalQuantityModifier).should().updateWeeklyQuantityAmount(command);
    }

    @Test
    void 주간_딜레이를_다른_주로_옮긴다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long receivingWeeklyQuantityId = 40L;
        int weeklyAmountToDelay = 10;
        String body = weeklyQuantityDelayRequestJson(receivingWeeklyQuantityId, weeklyAmountToDelay);
        WeeklyAmountDelayCommand command = WeeklyAmountDelayCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingWeeklyQuantityId,
                weeklyAmountToDelay
        );

        patchJson(
                userId,
                WEEKLY_QUANTITY_DELAYS,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(NO_CONTENT);

        then(totalQuantityModifier).should().delayWeeklyAmountTo(command);
    }

    @ParameterizedTest
    @MethodSource("invalidWeeklyQuantityDelayRequests")
    void 주간_딜레이_이동_요청값이_올바르지_않으면_400을_응답한다(String body) {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;

        patchJson(
                userId,
                WEEKLY_QUANTITY_DELAYS,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(BAD_REQUEST);

        then(totalQuantityModifier).should(never()).delayWeeklyAmountTo(any(WeeklyAmountDelayCommand.class));
    }

    @Test
    void 주간_딜레이_이동_시_세셔널_플랜이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long receivingWeeklyQuantityId = 40L;
        int weeklyAmountToDelay = 10;
        String body = weeklyQuantityDelayRequestJson(receivingWeeklyQuantityId, weeklyAmountToDelay);
        WeeklyAmountDelayCommand command = WeeklyAmountDelayCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingWeeklyQuantityId,
                weeklyAmountToDelay
        );
        willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(totalQuantityModifier)
                .delayWeeklyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY_DELAYS,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(NOT_FOUND);

        assertSessionalPlanNotFoundResponse(result);

        then(totalQuantityModifier).should().delayWeeklyAmountTo(command);
    }

    @Test
    void 주간_딜레이_이동_시_세션_총량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long receivingWeeklyQuantityId = 40L;
        int weeklyAmountToDelay = 10;
        String body = weeklyQuantityDelayRequestJson(receivingWeeklyQuantityId, weeklyAmountToDelay);
        WeeklyAmountDelayCommand command = WeeklyAmountDelayCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingWeeklyQuantityId,
                weeklyAmountToDelay
        );
        willThrow(new NotFoundException(TOTAL_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .delayWeeklyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY_DELAYS,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(NOT_FOUND);

        assertTotalQuantityNotFoundResponse(result);

        then(totalQuantityModifier).should().delayWeeklyAmountTo(command);
    }

    @Test
    void 주간_딜레이_이동_시_주간_계획량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long receivingWeeklyQuantityId = 40L;
        int weeklyAmountToDelay = 10;
        String body = weeklyQuantityDelayRequestJson(receivingWeeklyQuantityId, weeklyAmountToDelay);
        WeeklyAmountDelayCommand command = WeeklyAmountDelayCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingWeeklyQuantityId,
                weeklyAmountToDelay
        );
        willThrow(new NotFoundException(WEEKLY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .delayWeeklyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY_DELAYS,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(NOT_FOUND);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_WEEKLY_QUANTITY_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Weekly quantity not found.");

        then(totalQuantityModifier).should().delayWeeklyAmountTo(command);
    }

    @Test
    void 주간_딜레이_이동_시_같은_주로_옮기면_400을_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long receivingWeeklyQuantityId = 30L;
        int weeklyAmountToDelay = 10;
        String body = weeklyQuantityDelayRequestJson(receivingWeeklyQuantityId, weeklyAmountToDelay);
        WeeklyAmountDelayCommand command = WeeklyAmountDelayCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingWeeklyQuantityId,
                weeklyAmountToDelay
        );
        willThrow(new BadRequestException(WEEKLY_DELAY_RECEIVER_SAME_AS_SOURCE))
                .given(totalQuantityModifier)
                .delayWeeklyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY_DELAYS,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(BAD_REQUEST);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_400_WEEKLY_DELAY_RECEIVER_SAME_AS_SOURCE");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Weekly quantity receiving the delay must be different from source weekly quantity.");

        then(totalQuantityModifier).should().delayWeeklyAmountTo(command);
    }

    @Test
    void 주간_딜레이_이동_시_현재_주간_딜레이보다_많은_수량을_옮기면_400을_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        Long receivingWeeklyQuantityId = 40L;
        int weeklyAmountToDelay = 10;
        String body = weeklyQuantityDelayRequestJson(receivingWeeklyQuantityId, weeklyAmountToDelay);
        WeeklyAmountDelayCommand command = WeeklyAmountDelayCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingWeeklyQuantityId,
                weeklyAmountToDelay
        );
        willThrow(new BadRequestException(WEEKLY_DELAY_AMOUNT_EXCEEDS_CURRENT_WEEKLY_DELAYED_AMOUNT))
                .given(totalQuantityModifier)
                .delayWeeklyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY_DELAYS,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(BAD_REQUEST);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_400_WEEKLY_DELAY_AMOUNT_EXCEEDS_CURRENT_WEEKLY_DELAYED_AMOUNT");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Weekly delay amount exceeds current weekly delayed amount.");

        then(totalQuantityModifier).should().delayWeeklyAmountTo(command);
    }

    private static Stream<String> invalidWeeklyQuantityReduceRequests() {
        return Stream.of(
                """
                        {
                          "targetWeeklyQuantityAmount": null
                        }
                        """,
                weeklyQuantityReduceRequestJson(null, null),
                weeklyQuantityReduceRequestJson(-1, null),
                weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, "a".repeat(QUANTITY_MEMO_MAX_LENGTH + 1))
        );
    }

    private static Stream<String> invalidWeeklyQuantityDelayRequests() {
        return Stream.of(
                weeklyQuantityDelayRequestJson(null, TOTAL_AMOUNT),
                weeklyQuantityDelayRequestJson(30L, null),
                weeklyQuantityDelayRequestJson(-1L, TOTAL_AMOUNT),
                weeklyQuantityDelayRequestJson(30L, -1)
        );
    }

    private static String weeklyQuantityReduceRequestJson(Integer targetWeeklyAmount, String targetWeeklyMemo) {
        return """
                {
                  "targetWeeklyQuantityAmount": {
                    "quantityAmount": %s,
                    "quantityMemo": %s
                  }
                }
                """.formatted(
                targetWeeklyAmount,
                targetWeeklyMemo == null ? null : "\"" + targetWeeklyMemo + "\""
        );
    }

    private static String weeklyQuantityDelayRequestJson(Long receivingWeeklyQuantityId, Integer weeklyAmountToDelay) {
        return """
                {
                  "receivingWeeklyQuantityId": %s,
                  "weeklyAmountToDelay": %s
                }
                """.formatted(receivingWeeklyQuantityId, weeklyAmountToDelay);
    }

    private static WeeklyPlan.WeeklyPlanItem createWeeklyPlanItem(
            Long userId,
            Long sessionalPlanId,
            Long totalQuantityId,
            Long weeklyQuantityId
    ) {
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createPersistedTotalQuantity(totalQuantityId, sessionalPlan);
        WeeklyQuantity weeklyQuantity = totalQuantity.getWeeklyQuantities()
                .stream()
                .filter(candidate -> weeklyQuantityId.equals(candidate.getWeeklyQuantityId()))
                .findFirst()
                .orElseThrow();

        return new WeeklyPlan.WeeklyPlanItem(
                sessionalPlanId,
                totalQuantityId,
                totalQuantity.getSubject(),
                totalQuantity.getTotalQuantityAmount().quantityUnit(),
                weeklyQuantity
        );
    }

    private static void assertSessionalPlanNotFoundResponse(MvcTestResultAssert result) {
        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_SESSIONAL_PLAN_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Sessional plan not found.");
    }

    private static void assertTotalQuantityNotFoundResponse(MvcTestResultAssert result) {
        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_404_TOTAL_QUANTITY_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Total quantity not found.");
    }
}
