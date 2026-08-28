package net.akive.api.plan.adapter.webapi.totalquantity;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.adapter.webapi.common.dto.request.SubjectRequest;
import net.akive.api.plan.adapter.webapi.common.dto.request.TotalQuantityAmountRequest;
import net.akive.api.plan.adapter.webapi.totalquantity.dto.request.TotalQuantityCreateRequest;
import net.akive.api.plan.adapter.webapi.totalquantity.dto.request.TotalQuantityUpdateRequest;
import net.akive.api.plan.application.provided.totalquantity.dto.*;
import net.akive.api.plan.application.result.TotalQuantityProgress;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.support.PrivateApiTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BadRequestError.LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED;
import static net.akive.api.common.exception.business.BadRequestError.WEEKLY_QUANTITY_INCREASE_NOT_ALLOWED;
import static net.akive.api.common.exception.business.ConflictError.*;
import static net.akive.api.common.exception.business.NotFoundError.*;
import static net.akive.api.plan.domain.PlanFixture.*;
import static net.akive.api.plan.domain.policy.PlanPolicy.*;
import static net.akive.api.user.domain.UserFixture.createPersistedUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

class TotalQuantityApiTest extends PrivateApiTestSupport {

    private static final String TOTAL_QUANTITIES_URL = "/sessional-plans/{sessionalPlanId}/total-quantities";
    private static final String TOTAL_QUANTITY_URL = TOTAL_QUANTITIES_URL + "/{totalQuantityId}";
    private static final String WEEKLY_QUANTITY_URL = TOTAL_QUANTITY_URL + "/weekly-quantities/{weeklyQuantityId}";

    @Test
    void 세션_총량을_생성한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        TotalQuantityCreateRequest request = createRequest();
        TotalQuantityCreateCommand command = TotalQuantityCreateCommand.of(
                sessionalPlanId,
                userId,
                SUBJECT_NAME,
                SUBJECT_CATEGORY,
                TOTAL_AMOUNT,
                QUANTITY_UNIT,
                null
        );
        given(totalQuantityCreator.create(command)).willReturn(totalQuantityId);

        MvcTestResultAssert result = postJson(
                userId,
                TOTAL_QUANTITIES_URL,
                toJson(request),
                sessionalPlanId
        ).hasStatus(CREATED);

        result.bodyJson().extractingPath("$.totalQuantityId").isEqualTo(20);

        then(totalQuantityCreator).should().create(command);
    }

    @ParameterizedTest
    @MethodSource("invalidCreateRequests")
    void 세션_총량_생성_요청값이_올바르지_않으면_400을_응답한다(TotalQuantityCreateRequest request) {
        Long userId = 1L;
        Long sessionalPlanId = 10L;

        postJson(
                userId,
                TOTAL_QUANTITIES_URL,
                toJson(request),
                sessionalPlanId
        ).hasStatus(BAD_REQUEST);

        then(totalQuantityCreator).should(never()).create(any(TotalQuantityCreateCommand.class));
    }

    @Test
    void 세션_총량_생성_시_세셔널_플랜이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        TotalQuantityCreateRequest request = createRequest();
        TotalQuantityCreateCommand command = request.to(userId, sessionalPlanId);
        willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(totalQuantityCreator)
                .create(command);

        MvcTestResultAssert result = postJson(
                userId,
                TOTAL_QUANTITIES_URL,
                toJson(request),
                sessionalPlanId
        ).hasStatus(NOT_FOUND);

        assertSessionalPlanNotFoundResponse(result);

        then(totalQuantityCreator).should().create(command);
    }

    @Test
    void 세션_총량_생성_시_세션_과목이_중복되면_409를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        TotalQuantityCreateRequest request = createRequest();
        TotalQuantityCreateCommand command = request.to(userId, sessionalPlanId);
        willThrow(new ConflictException(SUBJECT_NAME_ALREADY_EXISTS))
                .given(totalQuantityCreator)
                .create(command);

        MvcTestResultAssert result = postJson(
                userId,
                TOTAL_QUANTITIES_URL,
                toJson(request),
                sessionalPlanId
        ).hasStatus(CONFLICT);

        assertSubjectNameAlreadyExistsResponse(result);

        then(totalQuantityCreator).should().create(command);
    }

    @Test
    void 세션_총량을_조회한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createPersistedTotalQuantity(totalQuantityId, sessionalPlan);
        TotalQuantityProgress totalQuantityProgress = TotalQuantityProgress.of(
                totalQuantity,
                sessionalPlan.getSessionPeriod().splitIntoWeeks()
        );
        SessionalPlanTotalQuantityFindQuery query = new SessionalPlanTotalQuantityFindQuery(
                sessionalPlanId,
                userId,
                totalQuantityId
        );
        given(totalQuantityFinder.find(query)).willReturn(totalQuantityProgress);

        MvcTestResultAssert result = get(
                userId,
                TOTAL_QUANTITY_URL,
                sessionalPlanId,
                totalQuantityId
        ).hasStatusOk();

        result.bodyJson().extractingPath("$.totalQuantityId").isEqualTo(20);
        result.bodyJson().extractingPath("$.subject.name").isEqualTo(SUBJECT_NAME);
        result.bodyJson().extractingPath("$.subject.category").isEqualTo(SUBJECT_CATEGORY);
        result.bodyJson().extractingPath("$.totalQuantityAmount.quantityAmount").isEqualTo(TOTAL_AMOUNT);
        result.bodyJson().extractingPath("$.totalQuantityAmount.quantityUnit").isEqualTo(QUANTITY_UNIT);
        result.bodyJson().extractingPath("$.totalQuantityAmount.quantityMemo").isNull();
        result.bodyJson().extractingPath("$.weeklyQuantities[0].weeklyQuantityId").isEqualTo(1);
        result.bodyJson().extractingPath("$.weeklyQuantities[0].sessionWeek.weekNum").isEqualTo(1);
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].weeklyQuantityAmount.quantityAmount")
                .isEqualTo(TOTAL_AMOUNT);
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].weeklyQuantityAmount.completedQuantityAmount")
                .isEqualTo(30);
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].weeklyQuantityAmount.delayedQuantityAmount")
                .isEqualTo(70);

        then(totalQuantityFinder).should().find(query);
    }

    @Test
    void 세션_총량_조회_시_세셔널_플랜이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        SessionalPlanTotalQuantityFindQuery query = new SessionalPlanTotalQuantityFindQuery(
                sessionalPlanId,
                userId,
                totalQuantityId
        );
        given(totalQuantityFinder.find(query))
                .willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND));

        MvcTestResultAssert result = get(
                userId,
                TOTAL_QUANTITY_URL,
                sessionalPlanId,
                totalQuantityId
        ).hasStatus(NOT_FOUND);

        assertSessionalPlanNotFoundResponse(result);

        then(totalQuantityFinder).should().find(query);
    }

    @Test
    void 세션_총량_조회_시_세션_총량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        SessionalPlanTotalQuantityFindQuery query = new SessionalPlanTotalQuantityFindQuery(
                sessionalPlanId,
                userId,
                totalQuantityId
        );
        given(totalQuantityFinder.find(query))
                .willThrow(new NotFoundException(TOTAL_QUANTITY_NOT_FOUND));

        MvcTestResultAssert result = get(
                userId,
                TOTAL_QUANTITY_URL,
                sessionalPlanId,
                totalQuantityId
        ).hasStatus(NOT_FOUND);

        assertTotalQuantityNotFoundResponse(result);

        then(totalQuantityFinder).should().find(query);
    }

    @Test
    void 세션_총량을_수정한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        TotalQuantityUpdateRequest request = updateRequest();
        TotalQuantityUpdateCommand command = request.to(userId, sessionalPlanId, totalQuantityId);

        putJson(
                userId,
                TOTAL_QUANTITY_URL,
                toJson(request),
                sessionalPlanId,
                totalQuantityId
        ).hasStatus(NO_CONTENT);

        then(totalQuantityModifier).should().update(command);
    }

    @ParameterizedTest
    @MethodSource("invalidUpdateRequests")
    void 세션_총량_수정_요청값이_올바르지_않으면_400을_응답한다(TotalQuantityUpdateRequest request) {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;

        putJson(
                userId,
                TOTAL_QUANTITY_URL,
                toJson(request),
                sessionalPlanId,
                totalQuantityId
        ).hasStatus(BAD_REQUEST);

        then(totalQuantityModifier).should(never()).update(any(TotalQuantityUpdateCommand.class));
    }

    @Test
    void 세션_총량_수정_시_세셔널_플랜이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        TotalQuantityUpdateRequest request = updateRequest();
        TotalQuantityUpdateCommand command = request.to(userId, sessionalPlanId, totalQuantityId);
        willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(totalQuantityModifier)
                .update(command);

        MvcTestResultAssert result = putJson(
                userId,
                TOTAL_QUANTITY_URL,
                toJson(request),
                sessionalPlanId,
                totalQuantityId
        ).hasStatus(NOT_FOUND);

        assertSessionalPlanNotFoundResponse(result);

        then(totalQuantityModifier).should().update(command);
    }

    @Test
    void 세션_총량_수정_시_세션_총량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        TotalQuantityUpdateRequest request = updateRequest();
        TotalQuantityUpdateCommand command = request.to(userId, sessionalPlanId, totalQuantityId);
        willThrow(new NotFoundException(TOTAL_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .update(command);

        MvcTestResultAssert result = putJson(
                userId,
                TOTAL_QUANTITY_URL,
                toJson(request),
                sessionalPlanId,
                totalQuantityId
        ).hasStatus(NOT_FOUND);

        assertTotalQuantityNotFoundResponse(result);

        then(totalQuantityModifier).should().update(command);
    }

    @Test
    void 세션_총량_수정_시_세션_과목이_중복되면_409를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        TotalQuantityUpdateRequest request = updateRequest();
        TotalQuantityUpdateCommand command = request.to(userId, sessionalPlanId, totalQuantityId);
        willThrow(new ConflictException(SUBJECT_NAME_ALREADY_EXISTS))
                .given(totalQuantityModifier)
                .update(command);

        MvcTestResultAssert result = putJson(
                userId,
                TOTAL_QUANTITY_URL,
                toJson(request),
                sessionalPlanId,
                totalQuantityId
        ).hasStatus(CONFLICT);

        assertSubjectNameAlreadyExistsResponse(result);

        then(totalQuantityModifier).should().update(command);
    }

    @Test
    void 세션_총량_수정_시_계획_달성량보다_적게_수정하면_409를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        TotalQuantityUpdateRequest request = updateRequest();
        TotalQuantityUpdateCommand command = request.to(userId, sessionalPlanId, totalQuantityId);
        willThrow(new ConflictException(TOTAL_QUANTITY_LESS_THAN_COMPLETED_QUANTITY))
                .given(totalQuantityModifier)
                .update(command);

        MvcTestResultAssert result = putJson(
                userId,
                TOTAL_QUANTITY_URL,
                toJson(request),
                sessionalPlanId,
                totalQuantityId
        ).hasStatus(CONFLICT);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_409_TOTAL_QUANTITY_LESS_THAN_COMPLETED_QUANTITY");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Total quantity cannot be less than completed quantity.");

        then(totalQuantityModifier).should().update(command);
    }

    @Test
    void 주간_계획량을_줄인다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityReduceCommand command = WeeklyQuantityReduceCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                TOTAL_AMOUNT,
                null
        );

        patchJson(
                userId,
                WEEKLY_QUANTITY_URL,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(NO_CONTENT);

        then(totalQuantityModifier).should().reduceWeeklyQuantity(command);
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
                WEEKLY_QUANTITY_URL,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(BAD_REQUEST);

        then(totalQuantityModifier).should(never()).reduceWeeklyQuantity(any(WeeklyQuantityReduceCommand.class));
    }

    @Test
    void 주간_계획량_수정_시_세셔널_플랜이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityReduceCommand command = WeeklyQuantityReduceCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(totalQuantityModifier)
                .reduceWeeklyQuantity(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY_URL,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(NOT_FOUND);

        assertSessionalPlanNotFoundResponse(result);

        then(totalQuantityModifier).should().reduceWeeklyQuantity(command);
    }

    @Test
    void 주간_계획량_수정_시_세션_총량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityReduceCommand command = WeeklyQuantityReduceCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new NotFoundException(TOTAL_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .reduceWeeklyQuantity(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY_URL,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(NOT_FOUND);

        assertTotalQuantityNotFoundResponse(result);

        then(totalQuantityModifier).should().reduceWeeklyQuantity(command);
    }

    @Test
    void 주간_계획량_수정_시_주간_계획량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityReduceCommand command = WeeklyQuantityReduceCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new NotFoundException(WEEKLY_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .reduceWeeklyQuantity(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY_URL,
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

        then(totalQuantityModifier).should().reduceWeeklyQuantity(command);
    }

    @Test
    void 주간_계획량_수정_시_마지막_주간_계획량을_줄이면_400을_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityReduceCommand command = WeeklyQuantityReduceCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new BadRequestException(LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED))
                .given(totalQuantityModifier)
                .reduceWeeklyQuantity(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY_URL,
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

        then(totalQuantityModifier).should().reduceWeeklyQuantity(command);
    }

    @Test
    void 주간_계획량_수정_시_주간_계획량을_늘리면_400을_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityReduceCommand command = WeeklyQuantityReduceCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new BadRequestException(WEEKLY_QUANTITY_INCREASE_NOT_ALLOWED))
                .given(totalQuantityModifier)
                .reduceWeeklyQuantity(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY_URL,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(BAD_REQUEST);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_400_WEEKLY_QUANTITY_INCREASE_NOT_ALLOWED");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Weekly quantity cannot be increased here.");

        then(totalQuantityModifier).should().reduceWeeklyQuantity(command);
    }

    @Test
    void 주간_계획량_수정_시_주간_계획_달성량보다_적게_수정하면_409를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        Long weeklyQuantityId = 30L;
        String body = weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, null);
        WeeklyQuantityReduceCommand command = WeeklyQuantityReduceCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                TOTAL_AMOUNT,
                null
        );
        willThrow(new ConflictException(WEEKLY_QUANTITY_LESS_THAN_COMPLETED_QUANTITY))
                .given(totalQuantityModifier)
                .reduceWeeklyQuantity(command);

        MvcTestResultAssert result = patchJson(
                userId,
                WEEKLY_QUANTITY_URL,
                body,
                sessionalPlanId,
                totalQuantityId,
                weeklyQuantityId
        ).hasStatus(CONFLICT);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_409_WEEKLY_QUANTITY_LESS_THAN_COMPLETED_QUANTITY");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Weekly quantity cannot be less than completed quantity.");

        then(totalQuantityModifier).should().reduceWeeklyQuantity(command);
    }

    @Test
    void 세션_총량을_삭제한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;

        delete(
                userId,
                TOTAL_QUANTITY_URL,
                sessionalPlanId,
                totalQuantityId
        ).hasStatus(NO_CONTENT);

        then(totalQuantityModifier).should()
                .delete(new TotalQuantityDeleteCommand(sessionalPlanId, userId, totalQuantityId));
    }

    @Test
    void 세션_총량_삭제_시_세셔널_플랜이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        TotalQuantityDeleteCommand command = new TotalQuantityDeleteCommand(
                sessionalPlanId,
                userId,
                totalQuantityId
        );
        willThrow(new NotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(totalQuantityModifier)
                .delete(command);

        MvcTestResultAssert result = delete(
                userId,
                TOTAL_QUANTITY_URL,
                sessionalPlanId,
                totalQuantityId
        ).hasStatus(NOT_FOUND);

        assertSessionalPlanNotFoundResponse(result);

        then(totalQuantityModifier).should().delete(command);
    }

    @Test
    void 세션_총량_삭제_시_세션_총량이_없으면_404를_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        TotalQuantityDeleteCommand command = new TotalQuantityDeleteCommand(
                sessionalPlanId,
                userId,
                totalQuantityId
        );
        willThrow(new NotFoundException(TOTAL_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .delete(command);

        MvcTestResultAssert result = delete(
                userId,
                TOTAL_QUANTITY_URL,
                sessionalPlanId,
                totalQuantityId
        ).hasStatus(NOT_FOUND);

        assertTotalQuantityNotFoundResponse(result);

        then(totalQuantityModifier).should().delete(command);
    }

    private static TotalQuantityCreateRequest createRequest() {
        return new TotalQuantityCreateRequest(
                new SubjectRequest(SUBJECT_NAME, SUBJECT_CATEGORY),
                new TotalQuantityAmountRequest(TOTAL_AMOUNT, QUANTITY_UNIT, null)
        );
    }

    private static TotalQuantityUpdateRequest updateRequest() {
        return new TotalQuantityUpdateRequest(
                new SubjectRequest(SUBJECT_NAME, SUBJECT_CATEGORY),
                new TotalQuantityAmountRequest(TOTAL_AMOUNT, QUANTITY_UNIT, null)
        );
    }

    private static Stream<TotalQuantityCreateRequest> invalidCreateRequests() {
        return invalidRequestValues()
                .map(value -> new TotalQuantityCreateRequest(value.subject(), value.totalQuantityAmount()));
    }

    private static Stream<TotalQuantityUpdateRequest> invalidUpdateRequests() {
        return invalidRequestValues()
                .map(value -> new TotalQuantityUpdateRequest(value.subject(), value.totalQuantityAmount()));
    }

    private static Stream<String> invalidWeeklyQuantityReduceRequests() {
        return Stream.of(
                """
                        {
                          "weeklyQuantityAmount": null
                        }
                        """,
                weeklyQuantityReduceRequestJson(null, null),
                weeklyQuantityReduceRequestJson(-1, null),
                weeklyQuantityReduceRequestJson(TOTAL_AMOUNT, "a".repeat(QUANTITY_MEMO_MAX_LENGTH + 1))
        );
    }

    private static String weeklyQuantityReduceRequestJson(Integer quantityAmount, String quantityMemo) {
        return """
                {
                  "weeklyQuantityAmount": {
                    "quantityAmount": %s,
                    "quantityMemo": %s
                  }
                }
                """.formatted(
                quantityAmount,
                quantityMemo == null ? null : "\"" + quantityMemo + "\""
        );
    }

    private static Stream<TotalQuantityRequestValue> invalidRequestValues() {
        return Stream.of(
                new TotalQuantityRequestValue(null, createTotalQuantityAmountRequest()),
                new TotalQuantityRequestValue(createSubjectRequest(), null),
                new TotalQuantityRequestValue(
                        new SubjectRequest("", SUBJECT_CATEGORY),
                        createTotalQuantityAmountRequest()
                ),
                new TotalQuantityRequestValue(
                        new SubjectRequest("a".repeat(SUBJECT_NAME_MAX_LENGTH + 1), SUBJECT_CATEGORY),
                        createTotalQuantityAmountRequest()
                ),
                new TotalQuantityRequestValue(
                        new SubjectRequest(SUBJECT_NAME, ""),
                        createTotalQuantityAmountRequest()
                ),
                new TotalQuantityRequestValue(
                        new SubjectRequest(SUBJECT_NAME, "a".repeat(SUBJECT_CATEGORY_MAX_LENGTH + 1)),
                        createTotalQuantityAmountRequest()
                ),
                new TotalQuantityRequestValue(
                        createSubjectRequest(),
                        new TotalQuantityAmountRequest(null, QUANTITY_UNIT, null)
                ),
                new TotalQuantityRequestValue(
                        createSubjectRequest(),
                        new TotalQuantityAmountRequest(-1, QUANTITY_UNIT, null)
                ),
                new TotalQuantityRequestValue(
                        createSubjectRequest(),
                        new TotalQuantityAmountRequest(TOTAL_AMOUNT, "", null)
                ),
                new TotalQuantityRequestValue(
                        createSubjectRequest(),
                        new TotalQuantityAmountRequest(
                                TOTAL_AMOUNT,
                                "a".repeat(QUANTITY_UNIT_MAX_LENGTH + 1),
                                null
                        )
                ),
                new TotalQuantityRequestValue(
                        createSubjectRequest(),
                        new TotalQuantityAmountRequest(
                                TOTAL_AMOUNT,
                                QUANTITY_UNIT,
                                "a".repeat(QUANTITY_MEMO_MAX_LENGTH + 1)
                        )
                )
        );
    }

    private static SubjectRequest createSubjectRequest() {
        return new SubjectRequest(SUBJECT_NAME, SUBJECT_CATEGORY);
    }

    private static TotalQuantityAmountRequest createTotalQuantityAmountRequest() {
        return new TotalQuantityAmountRequest(TOTAL_AMOUNT, QUANTITY_UNIT, null);
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

    private static void assertSubjectNameAlreadyExistsResponse(MvcTestResultAssert result) {
        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("PLAN_409_SUBJECT_NAME_ALREADY_EXISTS");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Subject name already exists.");
    }

    private record TotalQuantityRequestValue(
            SubjectRequest subject,
            TotalQuantityAmountRequest totalQuantityAmount
    ) {
    }
}
