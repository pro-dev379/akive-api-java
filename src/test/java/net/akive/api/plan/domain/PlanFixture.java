package net.akive.api.plan.domain;

import net.akive.api.plan.domain.model.DailyQuantity;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.model.WeeklyQuantity;
import net.akive.api.plan.domain.value.*;
import net.akive.api.user.domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static net.akive.api.user.domain.UserFixture.createPersistedUser;

public class PlanFixture {

    public static final String SESSION_NAME = "2026 1월 학습";
    public static final String SUBJECT_NAME = "수학";
    public static final String SUBJECT_CATEGORY = "공부";
    public static final int TOTAL_AMOUNT = 100;
    public static final String QUANTITY_UNIT = "문제";
    public static final LocalDateTime REQUESTED_AT =
            LocalDateTime.of(2025, 12, 31, 10, 0);

    public static SessionalPlan createSessionalPlan() {
        User user = createPersistedUser(1L);

        return createSessionalPlan(user);
    }

    public static SessionalPlan createSessionalPlan(User user) {
        return createSessionalPlan(user, SESSION_NAME);
    }

    public static SessionalPlan createSessionalPlan(User user, String sessionName) {
        return SessionalPlan.create(user, sessionName, createSessionPeriod(), REQUESTED_AT);
    }

    public static SessionalPlan createPersistedSessionalPlan() {
        User user = createPersistedUser(1L);

        return createPersistedSessionalPlan(10L, user);
    }

    public static SessionalPlan createPersistedSessionalPlan(Long sessionalPlanId, User user) {
        return createPersistedSessionalPlan(sessionalPlanId, user, SESSION_NAME, createSessionPeriod());
    }

    public static SessionalPlan createPersistedSessionalPlan(
            Long sessionalPlanId,
            User user,
            String sessionName,
            SessionPeriod sessionPeriod
    ) {
        return SessionalPlan.reconstruct(
                sessionalPlanId,
                user,
                sessionName,
                sessionPeriod
        );
    }

    public static SessionPeriod createSessionPeriod() {
        return new SessionPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 12)
        );
    }

    public static Subject createSubject() {
        return createSubject(SUBJECT_NAME);
    }

    public static Subject createSubject(String subjectName) {
        return new Subject(subjectName, SUBJECT_CATEGORY);
    }

    public static TotalQuantityAmount createTotalQuantityAmount() {
        return TotalQuantityAmount.of(TOTAL_AMOUNT, QUANTITY_UNIT, null);
    }

    public static TotalQuantity createTotalQuantity(SessionalPlan sessionalPlan) {
        return createTotalQuantity(sessionalPlan, SUBJECT_NAME);
    }

    public static TotalQuantity createTotalQuantity(SessionalPlan sessionalPlan, String subjectName) {
        return TotalQuantity.create(
                sessionalPlan,
                createSubject(subjectName),
                createTotalQuantityAmount()
        );
    }

    public static TotalQuantity createPersistedTotalQuantity(Long totalQuantityId, SessionalPlan sessionalPlan) {
        List<WeeklyQuantity> weeklyQuantities = sessionalPlan.getSessionPeriod()
                .splitIntoWeeks()
                .stream()
                .map(PlanFixture::createPersistedWeeklyQuantity)
                .toList();

        return TotalQuantity.reconstruct(
                totalQuantityId,
                sessionalPlan,
                createSubject(),
                createTotalQuantityAmount(),
                weeklyQuantities
        );
    }

    private static WeeklyQuantity createPersistedWeeklyQuantity(SessionWeek sessionWeek) {
        Long weeklyQuantityId = (long) sessionWeek.weekNum();
        WeekPeriod weekPeriod = sessionWeek.weekPeriod();
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(100, null);

        return WeeklyQuantity.reconstruct(
                weeklyQuantityId,
                weekPeriod,
                weeklyQuantityAmount,
                List.of(createPersistedDailyQuantity(weeklyQuantityId, weekPeriod))
        );
    }

    private static DailyQuantity createPersistedDailyQuantity(Long dailyQuantityId, WeekPeriod weekPeriod) {
        return DailyQuantity.reconstruct(
                dailyQuantityId,
                weekPeriod.startOn(),
                DailyQuantityAmount.of(100, 30, 70, null)
        );
    }
}
