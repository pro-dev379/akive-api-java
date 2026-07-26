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
        saveTotalQuantity(savedSessionalPlan, "수학");

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

    @Test
    void 세셔널_플랜에_과목명이_존재하는지_확인한다() {
        // given
        SessionalPlan savedSessionalPlan = saveSessionalPlan();
        saveTotalQuantity(savedSessionalPlan, "수학");

        // when
        boolean exists = validateTotalQuantityPort.existsBySessionalPlanIdAndSubjectName(
                savedSessionalPlan.getSessionalPlanId(),
                "수학"
        );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void 같은_과목명이어도_다른_세셔널_플랜이면_존재하지_않는_것으로_확인한다() {
        // given
        SessionalPlan savedSessionalPlan = saveSessionalPlan();
        SessionalPlan anotherSessionalPlan = saveSessionalPlan("2회차", "2");
        saveTotalQuantity(savedSessionalPlan, "수학");

        // when
        boolean exists = validateTotalQuantityPort.existsBySessionalPlanIdAndSubjectName(
                anotherSessionalPlan.getSessionalPlanId(),
                "수학"
        );

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void 세셔널_플랜의_세션_총량_개수를_조회한다() {
        // given
        SessionalPlan savedSessionalPlan = saveSessionalPlan();
        saveTotalQuantity(savedSessionalPlan, "수학");
        saveTotalQuantity(savedSessionalPlan, "영어");

        // when
        int totalQuantityCount = validateTotalQuantityPort.countBySessionalPlanId(
                savedSessionalPlan.getSessionalPlanId()
        );

        // then
        assertThat(totalQuantityCount).isEqualTo(2);
    }

    @Test
    void 존재하지_않는_세셔널_플랜_식별자로는_세션_총량_개수가_0이다() {
        // when
        int totalQuantityCount = validateTotalQuantityPort.countBySessionalPlanId(999L);

        // then
        assertThat(totalQuantityCount).isZero();
    }

    @Test
    void 세션_총량을_삭제한다() {
        // given
        SessionalPlan savedSessionalPlan = saveSessionalPlan();
        Long totalQuantityId = saveTotalQuantity(savedSessionalPlan, "수학");

        // when
        long deletedCount = deleteTotalQuantityPort.deleteByTotalQuantityIdAndSessionalPlanIdAndUserId(
                totalQuantityId,
                savedSessionalPlan.getSessionalPlanId(),
                savedSessionalPlan.getUserId()
        );

        // then
        assertThat(deletedCount).isEqualTo(1);
        assertThat(countTotalQuantities(totalQuantityId, savedSessionalPlan.getSessionalPlanId())).isZero();
        assertThat(countWeeklyQuantities(totalQuantityId)).isZero();
        assertThat(countDailyQuantitiesByTotalQuantityId(totalQuantityId)).isZero();
    }

    @Test
    void 삭제할_세션_총량이_없으면_0을_반환한다() {
        // given
        SessionalPlan savedSessionalPlan = saveSessionalPlan();
        Long notExistingTotalQuantityId = 999L;

        // when
        long deletedCount = deleteTotalQuantityPort.deleteByTotalQuantityIdAndSessionalPlanIdAndUserId(
                notExistingTotalQuantityId,
                savedSessionalPlan.getSessionalPlanId(),
                savedSessionalPlan.getUserId()
        );

        // then
        assertThat(deletedCount).isZero();
    }

    private SessionalPlan saveSessionalPlan() {
        return saveSessionalPlan("1회차", "1");
    }

    private SessionalPlan saveSessionalPlan(String sessionName, String suffix) {
        User savedUser = saveUserPort.save(User.signUpByEmail(
                new Email("test" + suffix + "@test.com"),
                "USER@abcd" + suffix,
                "password123!",
                USER
        ));

        return saveSessionalPlanPort.save(SessionalPlan.create(
                null,
                savedUser.getUserId(),
                sessionName,
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        ));
    }

    private Long saveTotalQuantity(SessionalPlan sessionalPlan, String subjectName) {
        TotalQuantityPlan totalQuantityPlan = totalQuantityPlanFactory.create(
                sessionalPlan,
                new Subject(subjectName, "과목"),
                TotalQuantityAmount.of(100, "문제", null)
        );
        return saveTotalQuantityPort.save(totalQuantityPlan);
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

    private Integer countDailyQuantitiesByTotalQuantityId(Long totalQuantityId) {
        return jdbcTemplate.queryForObject("""
                select count(*)
                from daily_quantity
                where weekly_quantity_id in (
                    select weekly_quantity_id
                    from weekly_quantity
                    where total_quantity_id = ?
                )
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
