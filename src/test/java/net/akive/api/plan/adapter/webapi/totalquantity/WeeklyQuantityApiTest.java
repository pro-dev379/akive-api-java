package net.akive.api.plan.adapter.webapi.totalquantity;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.adapter.webapi.common.PlanApiPath;
import net.akive.api.plan.adapter.webapi.totalquantity.fixture.WeeklyQuantityRequestFixture;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityModifier;
import net.akive.api.plan.application.provided.totalquantity.WeeklyQuantityFinder;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyAmountDelayCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyPlanItemFindQuery;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyQuantityAmountUpdateCommand;
import net.akive.api.plan.application.result.WeeklyPlan;
import net.akive.api.plan.domain.model.DailyQuantity;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.model.WeeklyQuantity;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.support.PrivateApiTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.common.exception.business.ResourceNotFoundError.*;
import static net.akive.api.plan.adapter.webapi.totalquantity.fixture.WeeklyQuantityRequestFixture.*;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.SESSIONAL_PLAN_ID;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.createReconstructedSessionalPlan;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.*;
import static net.akive.api.plan.domain.fixture.WeeklyQuantityFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(WeeklyQuantityApi.class)
class WeeklyQuantityApiTest extends PrivateApiTestSupport {

    @MockitoBean
    WeeklyQuantityFinder weeklyQuantityFinder;

    @MockitoBean
    TotalQuantityModifier totalQuantityModifier;

    @Test
    void 주간_계획량을_조회한다() {
        WeeklyPlan.WeeklyPlanItem weeklyPlanItem =
                createWeeklyPlanItem(USER_ID, SESSIONAL_PLAN_ID, FIRST_TOTAL_QUANTITY_ID, FIRST_WEEKLY_QUANTITY_ID);
        WeeklyPlanItemFindQuery query = new WeeklyPlanItemFindQuery(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        );
        given(weeklyQuantityFinder.findWeeklyPlanItem(query)).willReturn(weeklyPlanItem);

        MvcTestResultAssert result = get(
                PlanApiPath.WEEKLY_QUANTITY,
                USER_ID,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        ).hasStatusOk();

        assertWeeklyPlanItemResponse(result, weeklyPlanItem);

        then(weeklyQuantityFinder).should().findWeeklyPlanItem(query);
    }

    @Test
    void 주간_계획량이_없으면_404를_응답한다() {
        WeeklyPlanItemFindQuery query = new WeeklyPlanItemFindQuery(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        );
        given(weeklyQuantityFinder.findWeeklyPlanItem(query))
                .willThrow(new ResourceNotFoundException(WEEKLY_PLAN_ITEM_NOT_FOUND));

        MvcTestResultAssert result = get(
                PlanApiPath.WEEKLY_QUANTITY,
                USER_ID,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, WEEKLY_PLAN_ITEM_NOT_FOUND);

        then(weeklyQuantityFinder).should().findWeeklyPlanItem(query);
    }

    @Test
    void 주간_계획량을_줄인다() {
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityAmountUpdateCommand command = WeeklyQuantityAmountUpdateCommand.of(
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TOTAL_AMOUNT,
                null
        );

        patchJson(
                PlanApiPath.WEEKLY_QUANTITY,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        ).hasStatus(NO_CONTENT);

        then(totalQuantityModifier).should().updateWeeklyQuantityAmount(command);
    }

    @ParameterizedTest
    @MethodSource("invalidQuantityReduceRequests")
    void 주간_계획량_수정_요청값이_올바르지_않으면_400을_응답한다(String body) {
        patchJson(
                PlanApiPath.WEEKLY_QUANTITY,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        then(totalQuantityModifier)
                .should(never())
                .updateWeeklyQuantityAmount(any(WeeklyQuantityAmountUpdateCommand.class));
    }


    @Test
    void 주간_계획량_수정_시_세션_총량이_없으면_404를_응답한다() {
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityAmountUpdateCommand command = WeeklyQuantityAmountUpdateCommand.of(
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new ResourceNotFoundException(TOTAL_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateWeeklyQuantityAmount(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.WEEKLY_QUANTITY,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertTotalQuantityNotFoundResponse(result);

        then(totalQuantityModifier).should().updateWeeklyQuantityAmount(command);
    }

    @Test
    void 주간_계획량_수정_시_주간_계획량이_없으면_404를_응답한다() {
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityAmountUpdateCommand command = WeeklyQuantityAmountUpdateCommand.of(
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new ResourceNotFoundException(WEEKLY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .updateWeeklyQuantityAmount(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.WEEKLY_QUANTITY,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, WEEKLY_QUANTITY_NOT_FOUND);

        then(totalQuantityModifier).should().updateWeeklyQuantityAmount(command);
    }

    @Test
    void 주간_계획량_수정_시_마지막_주간_계획량을_줄이면_400을_응답한다() {
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityAmountUpdateCommand command = WeeklyQuantityAmountUpdateCommand.of(
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new BusinessRuleViolationException(LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED))
                .given(totalQuantityModifier)
                .updateWeeklyQuantityAmount(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.WEEKLY_QUANTITY,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED);

        then(totalQuantityModifier).should().updateWeeklyQuantityAmount(command);
    }

    @Test
    void 주간_계획량_수정_시_주간_계획량을_늘리면_400을_응답한다() {
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityAmountUpdateCommand command = WeeklyQuantityAmountUpdateCommand.of(
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new BusinessRuleViolationException(WEEKLY_AMOUNT_INCREASE_NOT_ALLOWED))
                .given(totalQuantityModifier)
                .updateWeeklyQuantityAmount(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.WEEKLY_QUANTITY,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, WEEKLY_AMOUNT_INCREASE_NOT_ALLOWED);

        then(totalQuantityModifier).should().updateWeeklyQuantityAmount(command);
    }

    @Test
    void 주간_계획량_수정_시_주간_계획_달성량보다_적게_수정하면_400을_응답한다() {
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityAmountUpdateCommand command = WeeklyQuantityAmountUpdateCommand.of(
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new BusinessRuleViolationException(WEEKLY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT))
                .given(totalQuantityModifier)
                .updateWeeklyQuantityAmount(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.WEEKLY_QUANTITY,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, WEEKLY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT);

        then(totalQuantityModifier).should().updateWeeklyQuantityAmount(command);
    }

    @Test
    void 주간_딜레이를_다른_주로_옮긴다() {
        String body = weeklyQuantityDelayRequestJson(SECOND_WEEKLY_QUANTITY_ID, WEEKLY_AMOUNT_TO_DELAY);
        WeeklyAmountDelayCommand command = WeeklyAmountDelayCommand.of(
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                SECOND_WEEKLY_QUANTITY_ID,
                WEEKLY_AMOUNT_TO_DELAY
        );

        patchJson(
                PlanApiPath.WEEKLY_QUANTITY_DELAYS,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        ).hasStatus(NO_CONTENT);

        then(totalQuantityModifier).should().delayWeeklyAmountTo(command);
    }

    @ParameterizedTest
    @MethodSource("invalidQuantityDelayRequests")
    void 주간_딜레이_이동_요청값이_올바르지_않으면_400을_응답한다(String body) {
        patchJson(
                PlanApiPath.WEEKLY_QUANTITY_DELAYS,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        then(totalQuantityModifier).should(never()).delayWeeklyAmountTo(any(WeeklyAmountDelayCommand.class));
    }


    @Test
    void 주간_딜레이_이동_시_세션_총량이_없으면_404를_응답한다() {
        String body = weeklyQuantityDelayRequestJson(SECOND_WEEKLY_QUANTITY_ID, WEEKLY_AMOUNT_TO_DELAY);
        WeeklyAmountDelayCommand command = WeeklyAmountDelayCommand.of(
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                SECOND_WEEKLY_QUANTITY_ID,
                WEEKLY_AMOUNT_TO_DELAY
        );
        willThrow(new ResourceNotFoundException(TOTAL_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .delayWeeklyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.WEEKLY_QUANTITY_DELAYS,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertTotalQuantityNotFoundResponse(result);

        then(totalQuantityModifier).should().delayWeeklyAmountTo(command);
    }

    @Test
    void 주간_딜레이_이동_시_주간_계획량이_없으면_404를_응답한다() {
        String body = weeklyQuantityDelayRequestJson(SECOND_WEEKLY_QUANTITY_ID, WEEKLY_AMOUNT_TO_DELAY);
        WeeklyAmountDelayCommand command = WeeklyAmountDelayCommand.of(
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                SECOND_WEEKLY_QUANTITY_ID,
                WEEKLY_AMOUNT_TO_DELAY
        );
        willThrow(new ResourceNotFoundException(WEEKLY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .delayWeeklyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.WEEKLY_QUANTITY_DELAYS,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, WEEKLY_QUANTITY_NOT_FOUND);

        then(totalQuantityModifier).should().delayWeeklyAmountTo(command);
    }

    @Test
    void 주간_딜레이_이동_시_같은_주로_옮기면_400을_응답한다() {
        String body = weeklyQuantityDelayRequestJson(FIRST_WEEKLY_QUANTITY_ID, WEEKLY_AMOUNT_TO_DELAY);
        WeeklyAmountDelayCommand command = WeeklyAmountDelayCommand.of(
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_WEEKLY_QUANTITY_ID,
                WEEKLY_AMOUNT_TO_DELAY
        );
        willThrow(new BusinessRuleViolationException(WEEKLY_DELAY_RECEIVER_SAME_AS_SOURCE))
                .given(totalQuantityModifier)
                .delayWeeklyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.WEEKLY_QUANTITY_DELAYS,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, WEEKLY_DELAY_RECEIVER_SAME_AS_SOURCE);

        then(totalQuantityModifier).should().delayWeeklyAmountTo(command);
    }

    @Test
    void 주간_딜레이_이동_시_현재_주간_딜레이보다_많은_수량을_옮기면_400을_응답한다() {
        String body = weeklyQuantityDelayRequestJson(SECOND_WEEKLY_QUANTITY_ID, WEEKLY_AMOUNT_TO_DELAY);
        WeeklyAmountDelayCommand command = WeeklyAmountDelayCommand.of(
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_TOTAL_QUANTITY_ID,
                SESSIONAL_PLAN_ID,
                USER_ID,
                SECOND_WEEKLY_QUANTITY_ID,
                WEEKLY_AMOUNT_TO_DELAY
        );
        willThrow(new BusinessRuleViolationException(WEEKLY_DELAY_AMOUNT_EXCEEDS_CURRENT_WEEKLY_DELAYED_AMOUNT))
                .given(totalQuantityModifier)
                .delayWeeklyAmountTo(command);

        MvcTestResultAssert result = patchJson(
                PlanApiPath.WEEKLY_QUANTITY_DELAYS,
                USER_ID,
                body,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID,
                FIRST_WEEKLY_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, WEEKLY_DELAY_AMOUNT_EXCEEDS_CURRENT_WEEKLY_DELAYED_AMOUNT);

        then(totalQuantityModifier).should().delayWeeklyAmountTo(command);
    }

    private static Stream<String> invalidQuantityReduceRequests() {
        return WeeklyQuantityRequestFixture.invalidQuantityReduceRequests();
    }

    private static Stream<String> invalidQuantityDelayRequests() {
        return WeeklyQuantityRequestFixture.invalidQuantityDelayRequests();
    }

    private static WeeklyPlan.WeeklyPlanItem createWeeklyPlanItem(
            Long userId,
            Long sessionalPlanId,
            Long totalQuantityId,
            Long weeklyQuantityId
    ) {
        User user = createReconstructedUser(userId);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createReconstructedTotalQuantity(totalQuantityId, sessionalPlan);
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

    private void assertTotalQuantityNotFoundResponse(MvcTestResultAssert result) {
        assertErrorResponse(result, TOTAL_QUANTITY_NOT_FOUND);
    }

    private void assertWeeklyPlanItemResponse(MvcTestResultAssert result, WeeklyPlan.WeeklyPlanItem weeklyPlanItem) {
        WeeklyQuantity weeklyQuantity = weeklyPlanItem.weeklyQuantity();
        DailyQuantity dailyQuantity = weeklyQuantity.getDailyQuantities().getFirst();
        DailyQuantityAmount dailyQuantityAmount = dailyQuantity.getDailyQuantityAmount();

        result.bodyJson()
                .extractingPath("$.sessionalPlanId")
                .isEqualTo(weeklyPlanItem.sessionalPlanId().intValue());
        result.bodyJson()
                .extractingPath("$.totalQuantityId")
                .isEqualTo(weeklyPlanItem.totalQuantityId().intValue());
        result.bodyJson()
                .extractingPath("$.weeklyQuantityId")
                .isEqualTo(weeklyQuantity.getWeeklyQuantityId().intValue());
        result.bodyJson().extractingPath("$.subject.name").isEqualTo(weeklyPlanItem.subject().name());
        result.bodyJson().extractingPath("$.subject.category").isEqualTo(weeklyPlanItem.subject().category());
        result.bodyJson()
                .extractingPath("$.weeklyQuantityAmount.quantityAmount")
                .isEqualTo(weeklyQuantity.getWeeklyQuantityAmount().quantityAmount().value());
        result.bodyJson()
                .extractingPath("$.weeklyQuantityAmount.quantityUnit")
                .isEqualTo(weeklyPlanItem.quantityUnit().value());
        result.bodyJson().extractingPath("$.weeklyQuantityAmount.quantityMemo").isNull();
        result.bodyJson()
                .extractingPath("$.dailyQuantities[0].dailyQuantityId")
                .isEqualTo(dailyQuantity.getDailyQuantityId().intValue());
        result.bodyJson()
                .extractingPath("$.dailyQuantities[0].planDate.date")
                .isEqualTo(dailyQuantity.getDate().toString());
        result.bodyJson()
                .extractingPath("$.dailyQuantities[0].dailyQuantityAmount.quantityAmount")
                .isEqualTo(dailyQuantityAmount.quantityAmount().value());
        result.bodyJson()
                .extractingPath("$.dailyQuantities[0].dailyQuantityAmount.completedQuantityAmount")
                .isEqualTo(dailyQuantityAmount.completedQuantityAmount().value());
        result.bodyJson()
                .extractingPath("$.dailyQuantities[0].dailyQuantityAmount.delayedQuantityAmount")
                .isEqualTo(dailyQuantityAmount.delayedQuantityAmount().value());
    }
}
