package net.akive.api.plan.domain.fixture;

import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.model.WeeklyQuantity;
import net.akive.api.plan.domain.value.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static net.akive.api.plan.domain.fixture.DailyQuantityFixture.createDailyQuantitiesFrom;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.createReconstructedSessionalPlan;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.createSessionalPlan;
import static net.akive.api.plan.domain.fixture.WeeklyQuantityFixture.*;

public class TotalQuantityFixture {

    public static final Long FIRST_TOTAL_QUANTITY_ID = 1L;
    public static final Long SECOND_TOTAL_QUANTITY_ID = 2L;
    public static final Long UNKNOWN_TOTAL_QUANTITY_ID = Long.MAX_VALUE;

    public static final String FIRST_SUBJECT_NAME = "수학";
    public static final String SECOND_SUBJECT_NAME = "국어";
    public static final String SUBJECT_CATEGORY = "수능";
    public static final String TARGET_SUBJECT_NAME = "영어";
    public static final String TARGET_SUBJECT_CATEGORY = "자기개발";
    public static final Subject TARGET_SUBJECT = new Subject(TARGET_SUBJECT_NAME, TARGET_SUBJECT_CATEGORY);

    public static final int TOTAL_AMOUNT = 100;
    public static final String QUANTITY_UNIT = "문제";
    public static final TotalQuantityAmount TARGET_TOTAL_QUANTITY_AMOUNT =
            TotalQuantityAmount.of(130, "페이지", "문법");
    public static final String TARGET_TOTAL_MEMO = "업데이트된 메모";
    public static final TotalQuantityAmount TARGET_REDUCED_TOTAL_QUANTITY_AMOUNT =
            TotalQuantityAmount.of(20, QUANTITY_UNIT);

    private TotalQuantityFixture() {
    }

    public static Subject createSubject() {
        return createSubject(FIRST_SUBJECT_NAME);
    }

    public static Subject createSubject(String subjectName) {
        return new Subject(subjectName, SUBJECT_CATEGORY);
    }

    public static TotalQuantityAmount createTotalQuantityAmount() {
        return createTotalQuantityAmount(TOTAL_AMOUNT);
    }

    public static TotalQuantityAmount createTotalQuantityAmount(int totalAmount) {
        return TotalQuantityAmount.of(totalAmount, QUANTITY_UNIT);
    }

    public static TotalQuantity createTotalQuantity(SessionalPlan sessionalPlan) {
        return createTotalQuantity(sessionalPlan, FIRST_SUBJECT_NAME);
    }

    public static TotalQuantity createTotalQuantity(SessionalPlan sessionalPlan, String subjectName) {
        return createTotalQuantity(sessionalPlan, subjectName, TOTAL_AMOUNT);
    }

    public static TotalQuantity createTotalQuantity(
            SessionalPlan sessionalPlan,
            String subjectName,
            int totalAmount
    ) {
        return TotalQuantity.create(
                sessionalPlan,
                createSubject(subjectName),
                createTotalQuantityAmount(totalAmount)
        );
    }

    public static TotalQuantity createReconstructedTotalQuantity(Long totalQuantityId) {
        return createReconstructedTotalQuantity(totalQuantityId, createSessionalPlan());
    }

    public static TotalQuantity createReconstructedTotalQuantity(Long totalQuantityId, SessionalPlan sessionalPlan) {
        return createReconstructedTotalQuantity(
                totalQuantityId,
                sessionalPlan,
                createSubject(),
                createTotalQuantityAmount()
        );
    }

    public static TotalQuantity createReconstructedTotalQuantity(Long totalQuantityId, TotalQuantity totalQuantity) {
        return createReconstructedTotalQuantity(
                totalQuantityId,
                totalQuantity.getSessionalPlan(),
                totalQuantity.getSubject(),
                totalQuantity.getTotalQuantityAmount()
        );
    }

    public static TotalQuantity createReconstructedTotalQuantity(
            Long totalQuantityId,
            SessionalPlan sessionalPlan,
            Subject subject,
            TotalQuantityAmount totalQuantityAmount
    ) {
        List<WeeklyQuantity> weeklyQuantities = sessionalPlan.getSessionPeriod()
                .splitIntoWeeks()
                .stream()
                .map(WeeklyQuantityFixture::createReconstructedWeeklyQuantity)
                .toList();

        return TotalQuantity.reconstruct(
                totalQuantityId,
                sessionalPlan,
                subject,
                totalQuantityAmount,
                weeklyQuantities
        );
    }

    public static TotalQuantity createTotalQuantityWithWeeklyAmounts(int... weeklyAmounts) {
        return createTotalQuantityWithWeeklyAmounts(FIRST_TOTAL_QUANTITY_ID, createSessionalPlan(), weeklyAmounts);
    }

    public static TotalQuantity createReconstructedTotalQuantityWithWeeklyAmounts(int... weeklyAmounts) {
        return createTotalQuantityWithWeeklyAmounts(
                FIRST_TOTAL_QUANTITY_ID,
                createReconstructedSessionalPlan(),
                weeklyAmounts
        );
    }

    public static TotalQuantity createTotalQuantityWithWeeklyAmounts(
            Long totalQuantityId,
            SessionalPlan sessionalPlan,
            int... weeklyAmounts
    ) {
        List<SessionWeek> sessionWeeks = sessionalPlan.getSessionPeriod().splitIntoWeeks();

        if (sessionWeeks.size() != weeklyAmounts.length) {
            throw new IllegalArgumentException("Weekly amount count must match session week count.");
        }

        List<WeeklyQuantity> weeklyQuantities = IntStream.range(0, weeklyAmounts.length)
                .mapToObj(index -> {
                    SessionWeek sessionWeek = sessionWeeks.get(index);
                    WeekPeriod weekPeriod = sessionWeek.weekPeriod();

                    return createWeeklyQuantity(
                            (long) sessionWeek.weekNum(),
                            weekPeriod.startOn(),
                            weekPeriod.endOn(),
                            weeklyAmounts[index]
                    );
                })
                .toList();
        int totalAmount = Arrays.stream(weeklyAmounts).sum();

        return TotalQuantity.reconstruct(
                totalQuantityId,
                sessionalPlan,
                createSubject(),
                TotalQuantityAmount.of(totalAmount, QUANTITY_UNIT),
                weeklyQuantities
        );
    }

    public static TotalQuantity createTotalQuantityWithCompletedAmount(
            int quantityAmount,
            int completedQuantityAmount
    ) {
        return TotalQuantity.reconstruct(
                FIRST_TOTAL_QUANTITY_ID,
                createSessionalPlan(),
                createSubject(),
                TotalQuantityAmount.of(quantityAmount, QUANTITY_UNIT),
                List.of(createWeeklyQuantityWithCompletedAmount(
                        FIRST_WEEKLY_QUANTITY_ID,
                        FIRST_WEEK_PERIOD.startOn(),
                        FIRST_WEEK_PERIOD.endOn(),
                        quantityAmount,
                        completedQuantityAmount
                ))
        );
    }

    public static TotalQuantity createTotalQuantityWithDailyAmounts(DailyQuantityAmount... dailyQuantityAmounts) {
        return createTotalQuantityWithDailyAmounts(
                FIRST_TOTAL_QUANTITY_ID,
                createSessionalPlan(),
                dailyQuantityAmounts
        );
    }

    public static TotalQuantity createTotalQuantityWithDailyAmounts(
            Long totalQuantityId,
            SessionalPlan sessionalPlan,
            DailyQuantityAmount... dailyQuantityAmounts
    ) {
        int totalAmount = Arrays.stream(dailyQuantityAmounts)
                .mapToInt(dailyQuantityAmount -> dailyQuantityAmount.quantityAmount().value())
                .sum();

        return TotalQuantity.reconstruct(
                totalQuantityId,
                sessionalPlan,
                createSubject(),
                TotalQuantityAmount.of(totalAmount, QUANTITY_UNIT),
                List.of(
                        WeeklyQuantity.reconstruct(
                                FIRST_WEEKLY_QUANTITY_ID,
                                FIRST_WEEK_PERIOD,
                                WeeklyQuantityAmount.of(totalAmount),
                                createDailyQuantitiesFrom(FIRST_WEEK_PERIOD.startOn(), dailyQuantityAmounts)
                        ),
                        createWeeklyQuantity(
                                SECOND_WEEKLY_QUANTITY_ID,
                                SECOND_WEEK_PERIOD.startOn(),
                                SECOND_WEEK_PERIOD.endOn(),
                                0
                        ),
                        createWeeklyQuantity(
                                THIRD_WEEKLY_QUANTITY_ID,
                                THIRD_WEEK_PERIOD.startOn(),
                                THIRD_WEEK_PERIOD.endOn(),
                                0
                        )
                )
        );
    }
}
