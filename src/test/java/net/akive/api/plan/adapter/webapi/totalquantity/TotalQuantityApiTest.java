package net.akive.api.plan.adapter.webapi.totalquantity;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.adapter.webapi.common.PlanApiPath;
import net.akive.api.plan.adapter.webapi.totalquantity.dto.request.TotalQuantityCreateRequest;
import net.akive.api.plan.adapter.webapi.totalquantity.dto.request.TotalQuantityUpdateRequest;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityCreator;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityFinder;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityModifier;
import net.akive.api.plan.application.provided.totalquantity.dto.SessionalPlanTotalQuantityFindQuery;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityCreateCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityDeleteCommand;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityUpdateCommand;
import net.akive.api.plan.application.result.TotalQuantityProgress;
import net.akive.api.plan.application.result.WeeklyQuantityProgress;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.value.TotalQuantityAmount;
import net.akive.api.support.PrivateApiTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.TOTAL_AMOUNT_LESS_THAN_COMPLETED_AMOUNT;
import static net.akive.api.common.exception.business.ResourceConflictError.SUBJECT_NAME_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.TOTAL_QUANTITY_NOT_FOUND;
import static net.akive.api.plan.adapter.webapi.totalquantity.fixture.TotalQuantityRequestFixture.*;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.SESSIONAL_PLAN_ID;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.createReconstructedSessionalPlan;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(TotalQuantityApi.class)
class TotalQuantityApiTest extends PrivateApiTestSupport {

    @MockitoBean
    TotalQuantityCreator totalQuantityCreator;

    @MockitoBean
    TotalQuantityFinder totalQuantityFinder;

    @MockitoBean
    TotalQuantityModifier totalQuantityModifier;

    @Test
    void 세션_총량을_생성한다() {
        TotalQuantityCreateRequest request = createTotalQuantityCreateRequest();
        TotalQuantityCreateCommand command = TotalQuantityCreateCommand.of(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_SUBJECT_NAME,
                SUBJECT_CATEGORY,
                TOTAL_AMOUNT,
                QUANTITY_UNIT,
                null
        );
        given(totalQuantityCreator.create(command)).willReturn(FIRST_TOTAL_QUANTITY_ID);

        MvcTestResultAssert result = postJson(
                PlanApiPath.TOTAL_QUANTITIES, 
                USER_ID,
                toJson(request),
                SESSIONAL_PLAN_ID
        ).hasStatus(CREATED);

        result.bodyJson()
                .extractingPath("$.totalQuantityId")
                .isEqualTo(FIRST_TOTAL_QUANTITY_ID.intValue());

        then(totalQuantityCreator).should().create(command);
    }

    @ParameterizedTest
    @MethodSource("invalidCreateRequests")
    void 세션_총량_생성_요청값이_올바르지_않으면_400을_응답한다(TotalQuantityCreateRequest request) {
        postJson(
                PlanApiPath.TOTAL_QUANTITIES, 
                USER_ID,
                toJson(request),
                SESSIONAL_PLAN_ID
        ).hasStatus(BAD_REQUEST);

        then(totalQuantityCreator).should(never()).create(any(TotalQuantityCreateCommand.class));
    }

    @Test
    void 세션_총량_생성_시_세셔널_플랜이_없으면_404를_응답한다() {
        TotalQuantityCreateRequest request = createTotalQuantityCreateRequest();
        TotalQuantityCreateCommand command = request.to(USER_ID, SESSIONAL_PLAN_ID);
        willThrow(new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(totalQuantityCreator)
                .create(command);

        MvcTestResultAssert result = postJson(
                PlanApiPath.TOTAL_QUANTITIES, 
                USER_ID,
                toJson(request),
                SESSIONAL_PLAN_ID
        ).hasStatus(NOT_FOUND);

        assertSessionalPlanNotFoundResponse(result);

        then(totalQuantityCreator).should().create(command);
    }

    @Test
    void 세션_총량_생성_시_세션_과목이_중복되면_409를_응답한다() {
        TotalQuantityCreateRequest request = createTotalQuantityCreateRequest();
        TotalQuantityCreateCommand command = request.to(USER_ID, SESSIONAL_PLAN_ID);
        willThrow(new ResourceConflictException(SUBJECT_NAME_ALREADY_EXISTS))
                .given(totalQuantityCreator)
                .create(command);

        MvcTestResultAssert result = postJson(
                PlanApiPath.TOTAL_QUANTITIES, 
                USER_ID,
                toJson(request),
                SESSIONAL_PLAN_ID
        ).hasStatus(CONFLICT);

        assertSubjectNameAlreadyExistsResponse(result);

        then(totalQuantityCreator).should().create(command);
    }

    @Test
    void 세션_총량을_조회한다() {
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, user);
        TotalQuantity totalQuantity = createReconstructedTotalQuantity(FIRST_TOTAL_QUANTITY_ID, sessionalPlan);
        TotalQuantityProgress totalQuantityProgress = TotalQuantityProgress.of(
                totalQuantity,
                sessionalPlan.getSessionPeriod().splitIntoWeeks()
        );
        SessionalPlanTotalQuantityFindQuery query = new SessionalPlanTotalQuantityFindQuery(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        );
        given(totalQuantityFinder.find(query)).willReturn(totalQuantityProgress);

        MvcTestResultAssert result = get(
                PlanApiPath.TOTAL_QUANTITY, 
                USER_ID,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID
        ).hasStatusOk();

        assertTotalQuantityProgressResponse(result, totalQuantityProgress);

        then(totalQuantityFinder).should().find(query);
    }


    @Test
    void 세션_총량_조회_시_세션_총량이_없으면_404를_응답한다() {
        SessionalPlanTotalQuantityFindQuery query = new SessionalPlanTotalQuantityFindQuery(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        );
        given(totalQuantityFinder.find(query))
                .willThrow(new ResourceNotFoundException(TOTAL_QUANTITY_NOT_FOUND));

        MvcTestResultAssert result = get(
                PlanApiPath.TOTAL_QUANTITY, 
                USER_ID,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertTotalQuantityNotFoundResponse(result);

        then(totalQuantityFinder).should().find(query);
    }

    @Test
    void 세션_총량을_수정한다() {
        TotalQuantityUpdateRequest request = createTotalQuantityUpdateRequest();
        TotalQuantityUpdateCommand command = request.to(USER_ID, SESSIONAL_PLAN_ID, FIRST_TOTAL_QUANTITY_ID);

        putJson(
                PlanApiPath.TOTAL_QUANTITY, 
                USER_ID,
                toJson(request),
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID
        ).hasStatus(NO_CONTENT);

        then(totalQuantityModifier).should().update(command);
    }

    @ParameterizedTest
    @MethodSource("invalidUpdateRequests")
    void 세션_총량_수정_요청값이_올바르지_않으면_400을_응답한다(TotalQuantityUpdateRequest request) {
        putJson(
                PlanApiPath.TOTAL_QUANTITY, 
                USER_ID,
                toJson(request),
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        then(totalQuantityModifier).should(never()).update(any(TotalQuantityUpdateCommand.class));
    }

    @Test
    void 세션_총량_수정_시_계획_달성량보다_적게_수정하면_400을_응답한다() {
        TotalQuantityUpdateRequest request = createTotalQuantityUpdateRequest();
        TotalQuantityUpdateCommand command = request.to(USER_ID, SESSIONAL_PLAN_ID, FIRST_TOTAL_QUANTITY_ID);
        willThrow(new BusinessRuleViolationException(TOTAL_AMOUNT_LESS_THAN_COMPLETED_AMOUNT))
                .given(totalQuantityModifier)
                .update(command);

        MvcTestResultAssert result = putJson(
                PlanApiPath.TOTAL_QUANTITY,
                USER_ID,
                toJson(request),
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, TOTAL_AMOUNT_LESS_THAN_COMPLETED_AMOUNT);

        then(totalQuantityModifier).should().update(command);
    }


    @Test
    void 세션_총량_수정_시_세션_총량이_없으면_404를_응답한다() {
        TotalQuantityUpdateRequest request = createTotalQuantityUpdateRequest();
        TotalQuantityUpdateCommand command = request.to(USER_ID, SESSIONAL_PLAN_ID, FIRST_TOTAL_QUANTITY_ID);
        willThrow(new ResourceNotFoundException(TOTAL_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .update(command);

        MvcTestResultAssert result = putJson(
                PlanApiPath.TOTAL_QUANTITY,
                USER_ID,
                toJson(request),
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertTotalQuantityNotFoundResponse(result);

        then(totalQuantityModifier).should().update(command);
    }

    @Test
    void 세션_총량_수정_시_세션_과목이_중복되면_409를_응답한다() {
        TotalQuantityUpdateRequest request = createTotalQuantityUpdateRequest();
        TotalQuantityUpdateCommand command = request.to(USER_ID, SESSIONAL_PLAN_ID, FIRST_TOTAL_QUANTITY_ID);
        willThrow(new ResourceConflictException(SUBJECT_NAME_ALREADY_EXISTS))
                .given(totalQuantityModifier)
                .update(command);

        MvcTestResultAssert result = putJson(
                PlanApiPath.TOTAL_QUANTITY,
                USER_ID,
                toJson(request),
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID
        ).hasStatus(CONFLICT);

        assertSubjectNameAlreadyExistsResponse(result);

        then(totalQuantityModifier).should().update(command);
    }

    @Test
    void 세션_총량을_삭제한다() {
        delete(
                PlanApiPath.TOTAL_QUANTITY,
                USER_ID,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID
        ).hasStatus(NO_CONTENT);

        then(totalQuantityModifier).should()
                .delete(new TotalQuantityDeleteCommand(SESSIONAL_PLAN_ID, USER_ID, FIRST_TOTAL_QUANTITY_ID));
    }


    @Test
    void 세션_총량_삭제_시_세션_총량이_없으면_404를_응답한다() {
        TotalQuantityDeleteCommand command = new TotalQuantityDeleteCommand(
                SESSIONAL_PLAN_ID,
                USER_ID,
                FIRST_TOTAL_QUANTITY_ID
        );
        willThrow(new ResourceNotFoundException(TOTAL_QUANTITY_NOT_FOUND))
                .given(totalQuantityModifier)
                .delete(command);

        MvcTestResultAssert result = delete(
                PlanApiPath.TOTAL_QUANTITY,
                USER_ID,
                SESSIONAL_PLAN_ID,
                FIRST_TOTAL_QUANTITY_ID
        ).hasStatus(NOT_FOUND);

        assertTotalQuantityNotFoundResponse(result);

        then(totalQuantityModifier).should().delete(command);
    }

    private static Stream<TotalQuantityCreateRequest> invalidCreateRequests() {
        return invalidTotalQuantityCreateRequests();
    }

    private static Stream<TotalQuantityUpdateRequest> invalidUpdateRequests() {
        return invalidTotalQuantityUpdateRequests();
    }

    private void assertSessionalPlanNotFoundResponse(MvcTestResultAssert result) {
        assertErrorResponse(result, SESSIONAL_PLAN_NOT_FOUND);
    }

    private void assertTotalQuantityNotFoundResponse(MvcTestResultAssert result) {
        assertErrorResponse(result, TOTAL_QUANTITY_NOT_FOUND);
    }

    private void assertSubjectNameAlreadyExistsResponse(MvcTestResultAssert result) {
        assertErrorResponse(result, SUBJECT_NAME_ALREADY_EXISTS);
    }

    private void assertTotalQuantityProgressResponse(
            MvcTestResultAssert result,
            TotalQuantityProgress totalQuantityProgress
    ) {
        TotalQuantityAmount totalQuantityAmount = totalQuantityProgress.totalQuantityAmount();
        WeeklyQuantityProgress weeklyQuantity = totalQuantityProgress.weeklyQuantities().getFirst();
        WeeklyQuantityProgress.WeeklyQuantityProgressAmount weeklyQuantityAmount =
                weeklyQuantity.weeklyQuantityAmount();

        result.bodyJson()
                .extractingPath("$.totalQuantityId")
                .isEqualTo(totalQuantityProgress.totalQuantityId().intValue());
        result.bodyJson()
                .extractingPath("$.subject.name")
                .isEqualTo(totalQuantityProgress.subject().name());
        result.bodyJson()
                .extractingPath("$.subject.category")
                .isEqualTo(totalQuantityProgress.subject().category());
        result.bodyJson()
                .extractingPath("$.totalQuantityAmount.quantityAmount")
                .isEqualTo(totalQuantityAmount.quantityAmount().value());
        result.bodyJson()
                .extractingPath("$.totalQuantityAmount.quantityUnit")
                .isEqualTo(totalQuantityAmount.quantityUnit().value());
        result.bodyJson().extractingPath("$.totalQuantityAmount.quantityMemo").isNull();
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].weeklyQuantityId")
                .isEqualTo(weeklyQuantity.weeklyQuantityId().intValue());
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].sessionWeek.weekNum")
                .isEqualTo(weeklyQuantity.sessionWeek().weekNum());
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].weeklyQuantityAmount.quantityAmount")
                .isEqualTo(weeklyQuantityAmount.weeklyQuantityAmount().quantityAmount().value());
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].weeklyQuantityAmount.completedQuantityAmount")
                .isEqualTo(weeklyQuantityAmount.completedQuantityAmount().value());
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].weeklyQuantityAmount.delayedQuantityAmount")
                .isEqualTo(weeklyQuantityAmount.delayedQuantityAmount().value());
    }
}
