package net.akive.api.plan.adapter.webapi.totalquantity.fixture;

import net.akive.api.plan.adapter.webapi.common.dto.request.SubjectRequest;
import net.akive.api.plan.adapter.webapi.common.dto.request.TotalQuantityAmountRequest;
import net.akive.api.plan.adapter.webapi.totalquantity.dto.request.TotalQuantityCreateRequest;
import net.akive.api.plan.adapter.webapi.totalquantity.dto.request.TotalQuantityUpdateRequest;

import java.util.stream.Stream;

import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.*;
import static net.akive.api.plan.domain.policy.PlanPolicy.*;

public class TotalQuantityRequestFixture {

    private TotalQuantityRequestFixture() {
    }

    public static TotalQuantityCreateRequest createTotalQuantityCreateRequest() {
        return new TotalQuantityCreateRequest(
                createSubjectRequest(),
                createTotalQuantityAmountRequest()
        );
    }

    public static TotalQuantityUpdateRequest createTotalQuantityUpdateRequest() {
        return new TotalQuantityUpdateRequest(
                createSubjectRequest(),
                createTotalQuantityAmountRequest()
        );
    }

    public static SubjectRequest createSubjectRequest() {
        return new SubjectRequest(FIRST_SUBJECT_NAME, SUBJECT_CATEGORY);
    }

    public static TotalQuantityAmountRequest createTotalQuantityAmountRequest() {
        return new TotalQuantityAmountRequest(TOTAL_AMOUNT, QUANTITY_UNIT, null);
    }

    public static Stream<TotalQuantityCreateRequest> invalidTotalQuantityCreateRequests() {
        return invalidRequestValues()
                .map(value -> new TotalQuantityCreateRequest(value.subject(), value.totalQuantityAmount()));
    }

    public static Stream<TotalQuantityUpdateRequest> invalidTotalQuantityUpdateRequests() {
        return invalidRequestValues()
                .map(value -> new TotalQuantityUpdateRequest(value.subject(), value.totalQuantityAmount()));
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
                        new SubjectRequest(FIRST_SUBJECT_NAME, ""),
                        createTotalQuantityAmountRequest()
                ),
                new TotalQuantityRequestValue(
                        new SubjectRequest(FIRST_SUBJECT_NAME, "a".repeat(SUBJECT_CATEGORY_MAX_LENGTH + 1)),
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

    private record TotalQuantityRequestValue(
            SubjectRequest subject,
            TotalQuantityAmountRequest totalQuantityAmount
    ) {
    }
}
