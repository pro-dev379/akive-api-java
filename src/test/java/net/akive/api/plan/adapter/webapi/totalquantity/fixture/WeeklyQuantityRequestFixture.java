package net.akive.api.plan.adapter.webapi.totalquantity.fixture;

import java.util.stream.Stream;

import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.TOTAL_AMOUNT;
import static net.akive.api.plan.domain.policy.PlanPolicy.QUANTITY_MEMO_MAX_LENGTH;

public class WeeklyQuantityRequestFixture {

    public static String weeklyQuantityReduceRequestJson(Integer targetWeeklyAmount, String targetWeeklyMemo) {
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

    public static String weeklyQuantityDelayRequestJson(Long receivingWeeklyQuantityId, Integer weeklyAmountToDelay) {
        return """
                {
                  "receivingWeeklyQuantityId": %s,
                  "weeklyAmountToDelay": %s
                }
                """.formatted(receivingWeeklyQuantityId, weeklyAmountToDelay);
    }

    public static Stream<String> invalidQuantityReduceRequests() {
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

    public static Stream<String> invalidQuantityDelayRequests() {
        return Stream.of(
                weeklyQuantityDelayRequestJson(null, TOTAL_AMOUNT),
                weeklyQuantityDelayRequestJson(30L, null),
                weeklyQuantityDelayRequestJson(-1L, TOTAL_AMOUNT),
                weeklyQuantityDelayRequestJson(30L, -1)
        );
    }
}
