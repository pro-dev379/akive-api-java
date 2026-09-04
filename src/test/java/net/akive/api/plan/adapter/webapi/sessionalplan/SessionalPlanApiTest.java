package net.akive.api.plan.adapter.webapi.sessionalplan;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.adapter.webapi.common.dto.request.SessionPeriodRequest;
import net.akive.api.plan.adapter.webapi.sessionalplan.dto.request.SessionalPlanCreateRequest;
import net.akive.api.plan.adapter.webapi.sessionalplan.dto.request.SessionalPlanUpdateRequest;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanCreateCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanDeleteCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanFindQuery;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanUpdateCommand;
import net.akive.api.plan.application.result.SessionalPlanDetail;
import net.akive.api.plan.application.result.TotalQuantityProgress;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.support.PrivateApiTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED;
import static net.akive.api.common.exception.business.ResourceConflictError.SESSION_NAME_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.plan.adapter.webapi.common.PlanApiPath.SESSIONAL_PLAN;
import static net.akive.api.plan.adapter.webapi.common.PlanApiPath.SESSIONAL_PLANS;
import static net.akive.api.plan.domain.PlanFixture.*;
import static net.akive.api.plan.domain.policy.PlanPolicy.SESSION_NAME_MAX_LENGTH;
import static net.akive.api.user.domain.UserFixture.createPersistedUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

class SessionalPlanApiTest extends PrivateApiTestSupport {

    @Test
    void 세셔널_플랜을_생성한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        SessionalPlanCreateCommand command = SessionalPlanCreateCommand.of(
                userId,
                SESSION_NAME,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 12)
        );
        given(sessionalPlanCreator.create(command)).willReturn(sessionalPlanId);

        MvcTestResultAssert result = postJson(userId, SESSIONAL_PLANS, toJson(createRequest()))
                .hasStatus(CREATED);

        result.bodyJson().extractingPath("$.sessionalPlanId").isEqualTo(10);

        then(sessionalPlanCreator).should().create(command);
    }

    @ParameterizedTest
    @MethodSource("invalidCreateRequests")
    void 세셔널_플랜_생성_요청값이_올바르지_않으면_400을_응답한다(
            SessionalPlanCreateRequest request
    ) {
        Long userId = 1L;

        postJson(userId, SESSIONAL_PLANS, toJson(request))
                .hasStatus(BAD_REQUEST);

        then(sessionalPlanCreator).should(never()).create(any(SessionalPlanCreateCommand.class));
    }

    @Test
    void 세셔널_플랜_생성_요청의_날짜_형식이_올바르지_않으면_400을_응답한다() {
        Long userId = 1L;
        String body = createInvalidDateFormatRequestJson();

        postJson(userId, SESSIONAL_PLANS, body)
                .hasStatus(BAD_REQUEST);

        then(sessionalPlanCreator).should(never()).create(any(SessionalPlanCreateCommand.class));
    }

    @Test
    void 세셔널_플랜_생성_요청_시_세셔널_플랜명이_중복되면_409를_응답한다() {
        Long userId = 1L;
        SessionalPlanCreateRequest request = createRequest();
        SessionalPlanCreateCommand command = request.to(userId);
        willThrow(new ResourceConflictException(SESSION_NAME_ALREADY_EXISTS))
                .given(sessionalPlanCreator)
                .create(command);

        MvcTestResultAssert result = postJson(userId, SESSIONAL_PLANS, toJson(request))
                .hasStatus(CONFLICT);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("SESSION_NAME_ALREADY_EXISTS");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Session name already exists.");

        then(sessionalPlanCreator).should().create(command);
    }

    @Test
    void 진행_중인_세셔널_플랜_개수_제한을_초과하면_400을_응답한다() {
        Long userId = 1L;
        SessionalPlanCreateRequest request = createRequest();
        SessionalPlanCreateCommand command = request.to(userId);
        willThrow(new BusinessRuleViolationException(IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED))
                .given(sessionalPlanCreator)
                .create(command);

        MvcTestResultAssert result = postJson(userId, SESSIONAL_PLANS, toJson(request))
                .hasStatus(BAD_REQUEST);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Too many in-progress sessional plans.");

        then(sessionalPlanCreator).should().create(command);
    }

    @Test
    void 세셔널_플랜_목록을_조회한다() {
        Long userId = 1L;
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(10L, createPersistedUser(userId));
        given(sessionalPlanFinder.findAll(userId)).willReturn(List.of(sessionalPlan));

        MvcTestResultAssert result = get(userId, SESSIONAL_PLANS)
                .hasStatusOk();

        result.bodyJson().extractingPath("$.sessionalPlans[0].sessionalPlanId")
                .isEqualTo(10);
        result.bodyJson().extractingPath("$.sessionalPlans[0].sessionName")
                .isEqualTo(SESSION_NAME);
        result.bodyJson().extractingPath("$.sessionalPlans[0].sessionPeriod.startOn")
                .isEqualTo("2026-01-01");
        result.bodyJson().extractingPath("$.sessionalPlans[0].sessionPeriod.endOn")
                .isEqualTo("2026-01-12");

        then(sessionalPlanFinder).should().findAll(userId);
    }

    @Test
    void 세셔널_플랜_상세를_조회한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createPersistedTotalQuantity(20L, sessionalPlan);
        SessionalPlanFindQuery query = new SessionalPlanFindQuery(userId, sessionalPlanId);
        SessionalPlanDetail detail = new SessionalPlanDetail(
                sessionalPlan,
                List.of(TotalQuantityProgress.of(
                        totalQuantity,
                        sessionalPlan.getSessionPeriod().splitIntoWeeks()
                ))
        );
        given(sessionalPlanFinder.find(query)).willReturn(detail);

        MvcTestResultAssert result = get(userId, SESSIONAL_PLAN, sessionalPlanId)
                .hasStatusOk();

        result.bodyJson().extractingPath("$.sessionalPlanId").isEqualTo(10);
        result.bodyJson().extractingPath("$.sessionName").isEqualTo(SESSION_NAME);
        result.bodyJson().extractingPath("$.sessionPeriod.startOn").isEqualTo("2026-01-01");
        result.bodyJson().extractingPath("$.sessionPeriod.endOn").isEqualTo("2026-01-12");
        result.bodyJson().extractingPath("$.sessionWeeks[0].weekNum").isEqualTo(1);
        result.bodyJson().extractingPath("$.sessionWeeks[0].startOn").isEqualTo("2026-01-01");
        result.bodyJson().extractingPath("$.sessionWeeks[0].endOn").isEqualTo("2026-01-04");
        result.bodyJson().extractingPath("$.totalQuantities[0].totalQuantityId").isEqualTo(20);
        result.bodyJson().extractingPath("$.totalQuantities[0].subject.name").isEqualTo(SUBJECT_NAME);
        result.bodyJson().extractingPath("$.totalQuantities[0].subject.category").isEqualTo(SUBJECT_CATEGORY);
        result.bodyJson()
                .extractingPath("$.totalQuantities[0].totalQuantityAmount.quantityAmount")
                .isEqualTo(TOTAL_AMOUNT);
        result.bodyJson()
                .extractingPath("$.totalQuantities[0].totalQuantityAmount.quantityUnit")
                .isEqualTo(QUANTITY_UNIT);
        result.bodyJson()
                .extractingPath("$.totalQuantities[0].weeklyQuantities[0].weeklyQuantityId")
                .isEqualTo(1);

        then(sessionalPlanFinder).should().find(query);
    }

    @Test
    void 세셔널_플랜_조회_시_세셔널_플랜이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 999L;
        SessionalPlanFindQuery query = new SessionalPlanFindQuery(userId, sessionalPlanId);
        given(sessionalPlanFinder.find(query))
                .willThrow(new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND));

        MvcTestResultAssert result = get(userId, SESSIONAL_PLAN, sessionalPlanId)
                .hasStatus(NOT_FOUND);

        assertSessionalPlanNotFoundResponse(result);

        then(sessionalPlanFinder).should().find(query);
    }

    @Test
    void 세셔널_플랜을_수정한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        SessionalPlanUpdateCommand command = SessionalPlanUpdateCommand.of(
                sessionalPlanId,
                userId,
                SESSION_NAME,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 12)
        );
        String body = toJson(createUpdateRequest());

        putJson(userId, SESSIONAL_PLAN, body, sessionalPlanId)
                .hasStatus(NO_CONTENT);

        then(sessionalPlanCreator).should().update(command);
    }

    @ParameterizedTest
    @MethodSource("invalidUpdateRequests")
    void 세셔널_플랜_수정_요청값이_올바르지_않으면_400을_응답한다(
            SessionalPlanUpdateRequest request
    ) {
        Long userId = 1L;
        Long sessionalPlanId = 10L;

        putJson(userId, SESSIONAL_PLAN, toJson(request), sessionalPlanId)
                .hasStatus(BAD_REQUEST);

        then(sessionalPlanCreator).should(never()).update(any(SessionalPlanUpdateCommand.class));
    }

    @Test
    void 세셔널_플랜_수정_요청의_날짜_형식이_올바르지_않으면_400을_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        String body = updateInvalidDateFormatRequestJson();

        putJson(userId, SESSIONAL_PLAN, body, sessionalPlanId)
                .hasStatus(BAD_REQUEST);

        then(sessionalPlanCreator).should(never()).update(any(SessionalPlanUpdateCommand.class));
    }

    @Test
    void 세셔널_플랜_수정_시_세셔널_플랜이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 999L;
        SessionalPlanUpdateRequest request = createUpdateRequest();
        SessionalPlanUpdateCommand command = request.to(sessionalPlanId, userId);
        willThrow(new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(sessionalPlanCreator)
                .update(command);

        MvcTestResultAssert result = putJson(
                userId,
                SESSIONAL_PLAN,
                toJson(request),
                sessionalPlanId
        ).hasStatus(NOT_FOUND);

        assertSessionalPlanNotFoundResponse(result);

        then(sessionalPlanCreator).should().update(command);
    }

    @Test
    void 세셔널_플랜을_삭제한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;

        delete(userId, SESSIONAL_PLAN, sessionalPlanId)
                .hasStatus(NO_CONTENT);

        then(sessionalPlanCreator).should().delete(new SessionalPlanDeleteCommand(userId, sessionalPlanId));
    }

    @Test
    void 세셔널_플랜_삭제_시_세셔널_플랜이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 999L;
        SessionalPlanDeleteCommand command = new SessionalPlanDeleteCommand(userId, sessionalPlanId);
        willThrow(new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(sessionalPlanCreator)
                .delete(command);

        MvcTestResultAssert result = delete(userId, SESSIONAL_PLAN, sessionalPlanId)
                .hasStatus(NOT_FOUND);

        assertSessionalPlanNotFoundResponse(result);

        then(sessionalPlanCreator).should().delete(command);
    }

    private static SessionalPlanCreateRequest createRequest() {
        return createRequest(SESSION_NAME);
    }

    private static SessionalPlanCreateRequest createRequest(String sessionName) {
        return new SessionalPlanCreateRequest(sessionName, createSessionPeriodRequest());
    }

    private static Stream<SessionalPlanCreateRequest> invalidCreateRequests() {
        return invalidRequestValues()
                .map(value -> new SessionalPlanCreateRequest(
                        value.sessionName(),
                        value.sessionPeriod()
                ));
    }

    private static Stream<SessionalPlanUpdateRequest> invalidUpdateRequests() {
        return invalidRequestValues()
                .map(value -> new SessionalPlanUpdateRequest(
                        value.sessionName(),
                        value.sessionPeriod()
                ));
    }

    private static Stream<SessionalPlanRequestValue> invalidRequestValues() {
        return Stream.of(
                new SessionalPlanRequestValue(null, createSessionPeriodRequest()),
                new SessionalPlanRequestValue("", createSessionPeriodRequest()),
                new SessionalPlanRequestValue(
                        "a".repeat(SESSION_NAME_MAX_LENGTH + 1),
                        createSessionPeriodRequest()
                ),
                new SessionalPlanRequestValue(SESSION_NAME, null),
                new SessionalPlanRequestValue(
                        SESSION_NAME,
                        new SessionPeriodRequest(null, LocalDate.of(2026, 1, 12))
                ),
                new SessionalPlanRequestValue(
                        SESSION_NAME,
                        new SessionPeriodRequest(LocalDate.of(2026, 1, 1), null)
                ),
                new SessionalPlanRequestValue(
                        SESSION_NAME,
                        new SessionPeriodRequest(
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 1, 1)
                        )
                ),
                new SessionalPlanRequestValue(
                        SESSION_NAME,
                        new SessionPeriodRequest(
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2999, 1, 1)
                        )
                )
        );
    }

    private SessionalPlanUpdateRequest createUpdateRequest() {
        return createUpdateRequest(createSessionPeriodRequest());
    }

    private SessionalPlanUpdateRequest createUpdateRequest(SessionPeriodRequest targetSessionPeriod) {
        return new SessionalPlanUpdateRequest(SESSION_NAME, targetSessionPeriod);
    }

    private static SessionPeriodRequest createSessionPeriodRequest() {
        SessionPeriod sessionPeriod = createSessionPeriod();

        return new SessionPeriodRequest(sessionPeriod.startOn(), sessionPeriod.endOn());
    }

    private static String createInvalidDateFormatRequestJson() {
        return """
                {
                  "sessionName": "%s",
                  "sessionPeriod": {
                    "startOn": "2026/01/01",
                    "endOn": "2026-02-01"
                  }
                }
                """.formatted(SESSION_NAME);
    }

    private static String updateInvalidDateFormatRequestJson() {
        return """
                {
                  "targetSessionName": "%s",
                  "targetSessionPeriod": {
                    "startOn": "2026/01/01",
                    "endOn": "2026-02-01"
                  }
                }
                """.formatted(SESSION_NAME);
    }

    private static void assertSessionalPlanNotFoundResponse(MvcTestResultAssert result) {
        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("SESSIONAL_PLAN_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Sessional plan not found.");
    }

    private record SessionalPlanRequestValue(
            String sessionName,
            SessionPeriodRequest sessionPeriod
    ) {
    }
}
