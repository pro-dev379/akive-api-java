package net.akive.api.plan.adapter.webapi.sessionalplan;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.adapter.webapi.common.PlanApiPath;
import net.akive.api.plan.adapter.webapi.sessionalplan.dto.request.SessionalPlanCreateRequest;
import net.akive.api.plan.adapter.webapi.sessionalplan.dto.request.SessionalPlanUpdateRequest;
import net.akive.api.plan.adapter.webapi.sessionalplan.fixture.SessionalPlanRequestFixture;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanCreator;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanFinder;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanCreateCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanDeleteCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanDetailFindQuery;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanUpdateCommand;
import net.akive.api.plan.application.result.SessionalPlanDetail;
import net.akive.api.plan.application.result.TotalQuantityProgress;
import net.akive.api.plan.application.result.WeeklyQuantityProgress;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.value.SessionWeek;
import net.akive.api.plan.domain.value.TotalQuantityAmount;
import net.akive.api.support.PrivateApiTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED;
import static net.akive.api.common.exception.business.ResourceConflictError.SESSION_NAME_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.plan.adapter.webapi.sessionalplan.fixture.SessionalPlanRequestFixture.*;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.*;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.FIRST_TOTAL_QUANTITY_ID;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.createReconstructedTotalQuantity;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(SessionalPlanApi.class)
class SessionalPlanApiTest extends PrivateApiTestSupport {

    @MockitoBean
    SessionalPlanCreator sessionalPlanCreator;

    @MockitoBean
    SessionalPlanFinder sessionalPlanFinder;

    @Test
    void 세셔널_플랜을_생성한다() {
        SessionalPlanCreateCommand command = SessionalPlanCreateCommand.of(
                USER_ID,
                SESSION_NAME,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 12)
        );
        given(sessionalPlanCreator.create(command)).willReturn(SESSIONAL_PLAN_ID);

        MvcTestResultAssert result = postJson(
                PlanApiPath.SESSIONAL_PLANS,
                USER_ID,
                toJson(createSessionalPlanCreateRequest())
        )
                .hasStatus(CREATED);

        result.bodyJson()
                .extractingPath("$.sessionalPlanId")
                .isEqualTo(SESSIONAL_PLAN_ID.intValue());

        then(sessionalPlanCreator).should().create(command);
    }

    @ParameterizedTest
    @MethodSource("invalidCreateRequests")
    void 세셔널_플랜_생성_요청값이_올바르지_않으면_400을_응답한다(
            SessionalPlanCreateRequest request
    ) {
        postJson(PlanApiPath.SESSIONAL_PLANS, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        then(sessionalPlanCreator).should(never()).create(any(SessionalPlanCreateCommand.class));
    }

    @Test
    void 세셔널_플랜_생성_요청의_날짜_형식이_올바르지_않으면_400을_응답한다() {
        String body = createInvalidDateFormatRequestJson();

        postJson(PlanApiPath.SESSIONAL_PLANS, USER_ID, body).hasStatus(BAD_REQUEST);

        then(sessionalPlanCreator).should(never()).create(any(SessionalPlanCreateCommand.class));
    }

    @Test
    void 세셔널_플랜_생성_요청_시_세셔널_플랜명이_중복되면_409를_응답한다() {
        SessionalPlanCreateRequest request = createSessionalPlanCreateRequest();
        SessionalPlanCreateCommand command = request.to(USER_ID);
        willThrow(new ResourceConflictException(SESSION_NAME_ALREADY_EXISTS))
                .given(sessionalPlanCreator)
                .create(command);

        MvcTestResultAssert result =
                postJson(PlanApiPath.SESSIONAL_PLANS, USER_ID, toJson(request)).hasStatus(CONFLICT);

        assertErrorResponse(result, SESSION_NAME_ALREADY_EXISTS);

        then(sessionalPlanCreator).should().create(command);
    }

    @Test
    void 진행_중인_세셔널_플랜_개수_제한을_초과하면_400을_응답한다() {
        SessionalPlanCreateRequest request = createSessionalPlanCreateRequest();
        SessionalPlanCreateCommand command = request.to(USER_ID);
        willThrow(new BusinessRuleViolationException(IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED))
                .given(sessionalPlanCreator)
                .create(command);

        MvcTestResultAssert result =
                postJson(PlanApiPath.SESSIONAL_PLANS, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED);

        then(sessionalPlanCreator).should().create(command);
    }

    @Test
    void 세셔널_플랜_목록을_조회한다() {
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(
                SESSIONAL_PLAN_ID,
                createReconstructedUser(USER_ID)
        );
        given(sessionalPlanFinder.findAll(USER_ID)).willReturn(List.of(sessionalPlan));

        MvcTestResultAssert result = get(PlanApiPath.SESSIONAL_PLANS, USER_ID).hasStatusOk();

        assertSessionalPlanSummaryResponse(result, sessionalPlan);

        then(sessionalPlanFinder).should().findAll(USER_ID);
    }

    @Test
    void 세셔널_플랜_상세를_조회한다() {
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, user);
        TotalQuantity totalQuantity = createReconstructedTotalQuantity(FIRST_TOTAL_QUANTITY_ID, sessionalPlan);
        SessionalPlanDetailFindQuery query = new SessionalPlanDetailFindQuery(USER_ID, SESSIONAL_PLAN_ID);
        SessionalPlanDetail detail = new SessionalPlanDetail(
                sessionalPlan,
                List.of(TotalQuantityProgress.of(
                        totalQuantity,
                        sessionalPlan.getSessionPeriod().splitIntoWeeks()
                ))
        );
        given(sessionalPlanFinder.findDetail(query)).willReturn(detail);

        MvcTestResultAssert result = get(PlanApiPath.SESSIONAL_PLAN, USER_ID, SESSIONAL_PLAN_ID).hasStatusOk();

        assertSessionalPlanDetailResponse(result, detail);

        then(sessionalPlanFinder).should().findDetail(query);
    }

    @Test
    void 세셔널_플랜_조회_시_세셔널_플랜이_없으면_404를_응답한다() {
        SessionalPlanDetailFindQuery query = new SessionalPlanDetailFindQuery(USER_ID, SESSIONAL_PLAN_ID);
        given(sessionalPlanFinder.findDetail(query))
                .willThrow(new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND));

        MvcTestResultAssert result = get(PlanApiPath.SESSIONAL_PLAN, USER_ID, SESSIONAL_PLAN_ID).hasStatus(NOT_FOUND);

        assertSessionalPlanNotFoundResponse(result);

        then(sessionalPlanFinder).should().findDetail(query);
    }

    @Test
    void 세셔널_플랜을_수정한다() {
        SessionalPlanUpdateCommand command = SessionalPlanUpdateCommand.of(
                SESSIONAL_PLAN_ID,
                USER_ID,
                SESSION_NAME,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 12)
        );
        String body = toJson(createSessionalPlanUpdateRequest());

        putJson(PlanApiPath.SESSIONAL_PLAN, USER_ID, body, SESSIONAL_PLAN_ID).hasStatus(NO_CONTENT);

        then(sessionalPlanCreator).should().update(command);
    }

    @ParameterizedTest
    @MethodSource("invalidUpdateRequests")
    void 세셔널_플랜_수정_요청값이_올바르지_않으면_400을_응답한다(SessionalPlanUpdateRequest request) {
        putJson(PlanApiPath.SESSIONAL_PLAN, USER_ID, toJson(request), SESSIONAL_PLAN_ID).hasStatus(BAD_REQUEST);

        then(sessionalPlanCreator).should(never()).update(any(SessionalPlanUpdateCommand.class));
    }

    @Test
    void 세셔널_플랜_수정_요청의_날짜_형식이_올바르지_않으면_400을_응답한다() {
        String body = updateInvalidDateFormatRequestJson();

        putJson(PlanApiPath.SESSIONAL_PLAN, USER_ID, body, SESSIONAL_PLAN_ID).hasStatus(BAD_REQUEST);

        then(sessionalPlanCreator).should(never()).update(any(SessionalPlanUpdateCommand.class));
    }

    @Test
    void 세셔널_플랜_수정_시_세셔널_플랜이_없으면_404를_응답한다() {
        SessionalPlanUpdateRequest request = createSessionalPlanUpdateRequest();
        SessionalPlanUpdateCommand command = request.to(SESSIONAL_PLAN_ID, USER_ID);
        willThrow(new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(sessionalPlanCreator)
                .update(command);

        MvcTestResultAssert result = putJson(
                PlanApiPath.SESSIONAL_PLAN,
                USER_ID,
                toJson(request),
                SESSIONAL_PLAN_ID
        ).hasStatus(NOT_FOUND);

        assertSessionalPlanNotFoundResponse(result);

        then(sessionalPlanCreator).should().update(command);
    }

    @Test
    void 세셔널_플랜을_삭제한다() {
        delete(PlanApiPath.SESSIONAL_PLAN, USER_ID, SESSIONAL_PLAN_ID).hasStatus(NO_CONTENT);

        then(sessionalPlanCreator).should().delete(new SessionalPlanDeleteCommand(USER_ID, SESSIONAL_PLAN_ID));
    }

    @Test
    void 세셔널_플랜_삭제_시_세셔널_플랜이_없으면_404를_응답한다() {
        SessionalPlanDeleteCommand command = new SessionalPlanDeleteCommand(USER_ID, SESSIONAL_PLAN_ID);
        willThrow(new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(sessionalPlanCreator)
                .delete(command);

        MvcTestResultAssert result = delete(PlanApiPath.SESSIONAL_PLAN, USER_ID, SESSIONAL_PLAN_ID)
                .hasStatus(NOT_FOUND);

        assertSessionalPlanNotFoundResponse(result);

        then(sessionalPlanCreator).should().delete(command);
    }

    private static Stream<SessionalPlanCreateRequest> invalidCreateRequests() {
        return SessionalPlanRequestFixture.invalidCreateRequests();
    }

    private static Stream<SessionalPlanUpdateRequest> invalidUpdateRequests() {
        return SessionalPlanRequestFixture.invalidUpdateRequests();
    }

    private void assertSessionalPlanNotFoundResponse(MvcTestResultAssert result) {
        assertErrorResponse(result, SESSIONAL_PLAN_NOT_FOUND);
    }

    private void assertSessionalPlanSummaryResponse(MvcTestResultAssert result, SessionalPlan sessionalPlan) {
        result.bodyJson()
                .extractingPath("$.sessionalPlans[0].sessionalPlanId")
                .isEqualTo(sessionalPlan.getSessionalPlanId().intValue());
        result.bodyJson()
                .extractingPath("$.sessionalPlans[0].sessionName")
                .isEqualTo(sessionalPlan.getSessionName());
        result.bodyJson()
                .extractingPath("$.sessionalPlans[0].sessionPeriod.startOn")
                .isEqualTo(sessionalPlan.getSessionPeriod().startOn().toString());
        result.bodyJson()
                .extractingPath("$.sessionalPlans[0].sessionPeriod.endOn")
                .isEqualTo(sessionalPlan.getSessionPeriod().endOn().toString());
    }

    private void assertSessionalPlanDetailResponse(MvcTestResultAssert result, SessionalPlanDetail detail) {
        SessionalPlan sessionalPlan = detail.sessionalPlan();
        SessionWeek firstSessionWeek = sessionalPlan.getSessionPeriod().splitIntoWeeks().getFirst();
        TotalQuantityProgress totalQuantity = detail.totalQuantities().getFirst();
        TotalQuantityAmount totalQuantityAmount = totalQuantity.totalQuantityAmount();
        WeeklyQuantityProgress weeklyQuantity = totalQuantity.weeklyQuantities().getFirst();

        result.bodyJson()
                .extractingPath("$.sessionalPlanId")
                .isEqualTo(sessionalPlan.getSessionalPlanId().intValue());
        result.bodyJson().extractingPath("$.sessionName").isEqualTo(sessionalPlan.getSessionName());
        result.bodyJson()
                .extractingPath("$.sessionPeriod.startOn")
                .isEqualTo(sessionalPlan.getSessionPeriod().startOn().toString());
        result.bodyJson()
                .extractingPath("$.sessionPeriod.endOn")
                .isEqualTo(sessionalPlan.getSessionPeriod().endOn().toString());
        result.bodyJson().extractingPath("$.sessionWeeks[0].weekNum").isEqualTo(firstSessionWeek.weekNum());
        result.bodyJson()
                .extractingPath("$.sessionWeeks[0].startOn")
                .isEqualTo(firstSessionWeek.weekPeriod().startOn().toString());
        result.bodyJson()
                .extractingPath("$.sessionWeeks[0].endOn")
                .isEqualTo(firstSessionWeek.weekPeriod().endOn().toString());
        result.bodyJson()
                .extractingPath("$.totalQuantities[0].totalQuantityId")
                .isEqualTo(totalQuantity.totalQuantityId().intValue());
        result.bodyJson()
                .extractingPath("$.totalQuantities[0].subject.name")
                .isEqualTo(totalQuantity.subject().name());
        result.bodyJson()
                .extractingPath("$.totalQuantities[0].subject.category")
                .isEqualTo(totalQuantity.subject().category());
        result.bodyJson()
                .extractingPath("$.totalQuantities[0].totalQuantityAmount.quantityAmount")
                .isEqualTo(totalQuantityAmount.quantityAmount().value());
        result.bodyJson()
                .extractingPath("$.totalQuantities[0].totalQuantityAmount.quantityUnit")
                .isEqualTo(totalQuantityAmount.quantityUnit().value());
        result.bodyJson()
                .extractingPath("$.totalQuantities[0].weeklyQuantities[0].weeklyQuantityId")
                .isEqualTo(weeklyQuantity.weeklyQuantityId().intValue());
    }
}
