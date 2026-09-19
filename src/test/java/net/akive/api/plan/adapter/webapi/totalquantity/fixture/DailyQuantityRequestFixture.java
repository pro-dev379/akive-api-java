package net.akive.api.plan.adapter.webapi.totalquantity.fixture;

import java.util.stream.Stream;

import static net.akive.api.plan.domain.policy.PlanPolicy.QUANTITY_MEMO_MAX_LENGTH;

public class DailyQuantityRequestFixture {

    private DailyQuantityRequestFixture() {
    }

    public static String dailyAmountUpdateRequestJson(Integer targetDailyAmount, String targetDailyMemo) {
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

    public static String dailyCompletedAmountUpdateRequestJson(Integer targetDailyCompletedAmount) {
        return """
                {
                  "targetDailyCompletedAmount": %s
                }
                """.formatted(targetDailyCompletedAmount);
    }

    public static String dailyAmountDelayRequestJson(Long receivingDailyQuantityId, Integer dailyAmountToDelay) {
        return """
                {
                  "receivingDailyQuantityId": %s,
                  "dailyAmountToDelay": %s
                }
                """.formatted(receivingDailyQuantityId, dailyAmountToDelay);
    }

    public static Stream<String> invalidAmountUpdateRequests() {
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

    public static Stream<String> invalidCompletedAmountUpdateRequests() {
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

    public static Stream<String> invalidAmountDelayRequests() {
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
}
