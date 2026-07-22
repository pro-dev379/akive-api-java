package net.akive.api.plan.infrastructure.adapter;

import net.akive.api.common.domain.Email;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.Subject;
import net.akive.api.plan.domain.TotalQuantityAmount;
import net.akive.api.plan.domain.TotalQuantityPlan;
import net.akive.api.plan.domain.TotalQuantityPlanFactory;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;

import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;

class TotalQuantityJpaAdapterTest extends RepositoryTestSupport {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private final TotalQuantityPlanFactory totalQuantityPlanFactory = new TotalQuantityPlanFactory();

    @Test
    void 세션_총량_계획을_저장한다() {
        // given
        SessionalPlan savedSessionalPlan = saveSessionalPlan();
        TotalQuantityPlan totalQuantityPlan = totalQuantityPlanFactory.create(
                savedSessionalPlan,
                new Subject("수학", "과목"),
                TotalQuantityAmount.of(100, "문제", "총량 메모")
        );

        // when
        Long savedTotalQuantityId = saveTotalQuantityPort.save(totalQuantityPlan);

        // then
        assertThat(savedTotalQuantityId).isNotNull();
        assertThat(countTotalQuantities(savedTotalQuantityId, savedSessionalPlan.getSessionalPlanId())).isEqualTo(1);
        assertThat(loadTotalQuantityAmount(savedTotalQuantityId)).isEqualTo(100);
        assertThat(loadTotalQuantityUnit(savedTotalQuantityId)).isEqualTo("문제");

        assertThat(countWeeklyQuantities(savedTotalQuantityId)).isEqualTo(2);
        assertThat(loadWeeklyQuantityAmount(savedTotalQuantityId, LocalDate.of(2026, 7, 7))).isEqualTo(100);
        assertThat(loadWeeklyQuantityAmount(savedTotalQuantityId, LocalDate.of(2026, 7, 13))).isZero();

        assertThat(countDailyQuantities(savedTotalQuantityId)).isEqualTo(8);
        assertThat(loadDailyQuantityAmount(savedTotalQuantityId, LocalDate.of(2026, 7, 7))).isEqualTo(100);
        assertThat(loadCompletedQuantityAmount(savedTotalQuantityId, LocalDate.of(2026, 7, 7))).isZero();
        assertThat(loadDelayedQuantityAmount(savedTotalQuantityId, LocalDate.of(2026, 7, 7))).isEqualTo(100);
        assertThat(loadDailyQuantityAmount(savedTotalQuantityId, LocalDate.of(2026, 7, 14))).isZero();
    }

    @Test
    void 세셔널_플랜에_세션_총량이_존재하는지_확인한다() {
        // given
        SessionalPlan savedSessionalPlan = saveSessionalPlan();
        TotalQuantityPlan totalQuantityPlan = totalQuantityPlanFactory.create(
                savedSessionalPlan,
                new Subject("수학", "과목"),
                TotalQuantityAmount.of(100, "문제", null)
        );
        saveTotalQuantityPort.save(totalQuantityPlan);

        // when
        boolean exists = validateTotalQuantityPort.existsBySessionalPlanId(savedSessionalPlan.getSessionalPlanId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void 존재하지_않는_세셔널_플랜_식별자로는_세션_총량을_조회할_수_없다() {
        // when
        boolean exists = validateTotalQuantityPort.existsBySessionalPlanId(999L);

        // then
        assertThat(exists).isFalse();
    }

    private SessionalPlan saveSessionalPlan() {
        User savedUser = saveUserPort.save(User.signUpByEmail(
                new Email("test@test.com"),
                "USER@abcde",
                "password123!",
                USER
        ));

        return saveSessionalPlanPort.save(SessionalPlan.create(
                null,
                savedUser.getUserId(),
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        ));
    }

    private Integer countTotalQuantities(Long totalQuantityId, Long sessionalPlanId) {
        return jdbcTemplate.queryForObject("""
                select count(*)
                from total_quantity
                where total_quantity_id = ?
                  and sessional_plan_id = ?
                """, Integer.class, totalQuantityId, sessionalPlanId);
    }

    private Integer loadTotalQuantityAmount(Long totalQuantityId) {
        return jdbcTemplate.queryForObject("""
                select total_quantity_amount
                from total_quantity
                where total_quantity_id = ?
                """, Integer.class, totalQuantityId);
    }

    private String loadTotalQuantityUnit(Long totalQuantityId) {
        return jdbcTemplate.queryForObject("""
                select unit
                from total_quantity
                where total_quantity_id = ?
                """, String.class, totalQuantityId);
    }

    private Integer countWeeklyQuantities(Long totalQuantityId) {
        return jdbcTemplate.queryForObject("""
                select count(*)
                from weekly_quantity
                where total_quantity_id = ?
                """, Integer.class, totalQuantityId);
    }

    private Integer loadWeeklyQuantityAmount(Long totalQuantityId, LocalDate weekStartOn) {
        return jdbcTemplate.queryForObject("""
                select weekly_quantity_amount
                from weekly_quantity
                where total_quantity_id = ?
                  and week_start_on = ?
                """, Integer.class, totalQuantityId, weekStartOn);
    }

    private Integer countDailyQuantities(Long totalQuantityId) {
        return jdbcTemplate.queryForObject("""
                select count(*)
                from daily_quantity dq
                join weekly_quantity wq on wq.weekly_quantity_id = dq.weekly_quantity_id
                where wq.total_quantity_id = ?
                """, Integer.class, totalQuantityId);
    }

    private Integer loadDailyQuantityAmount(Long totalQuantityId, LocalDate date) {
        return jdbcTemplate.queryForObject("""
                select dq.daily_quantity_amount
                from daily_quantity dq
                join weekly_quantity wq on wq.weekly_quantity_id = dq.weekly_quantity_id
                where wq.total_quantity_id = ?
                  and dq.date = ?
                """, Integer.class, totalQuantityId, date);
    }

    private Integer loadCompletedQuantityAmount(Long totalQuantityId, LocalDate date) {
        return jdbcTemplate.queryForObject("""
                select dq.completed_quantity_amount
                from daily_quantity dq
                join weekly_quantity wq on wq.weekly_quantity_id = dq.weekly_quantity_id
                where wq.total_quantity_id = ?
                  and dq.date = ?
                """, Integer.class, totalQuantityId, date);
    }

    private Integer loadDelayedQuantityAmount(Long totalQuantityId, LocalDate date) {
        return jdbcTemplate.queryForObject("""
                select dq.delayed_quantity_amount
                from daily_quantity dq
                join weekly_quantity wq on wq.weekly_quantity_id = dq.weekly_quantity_id
                where wq.total_quantity_id = ?
                  and dq.date = ?
                """, Integer.class, totalQuantityId, date);
    }
}
