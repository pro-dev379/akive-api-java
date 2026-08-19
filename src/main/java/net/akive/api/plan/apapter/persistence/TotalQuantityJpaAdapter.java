package net.akive.api.plan.apapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.apapter.persistence.entity.TotalQuantityEntity;
import net.akive.api.plan.application.required.DeleteTotalQuantityPort;
import net.akive.api.plan.application.required.SaveTotalQuantityPort;
import net.akive.api.plan.application.required.ValidateTotalQuantityPort;
import net.akive.api.plan.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
class TotalQuantityJpaAdapter implements SaveTotalQuantityPort, DeleteTotalQuantityPort, ValidateTotalQuantityPort {

    private static final int WEEKLY_QUANTITY_INSERT_CHUNK_SIZE = 100;
    private static final int DAILY_QUANTITY_INSERT_CHUNK_SIZE = 500;
    private static final String WEEKLY_QUANTITY_INSERT_SQL = """
            insert into weekly_quantity (
                total_quantity_id,
                week_start_on,
                week_end_on,
                weekly_quantity_amount,
                weekly_quantity_memo,
                created_at,
                updated_at
            ) values (?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String DAILY_QUANTITY_INSERT_SQL = """
            insert into daily_quantity (
                weekly_quantity_id,
                date,
                daily_quantity_amount,
                completed_quantity_amount,
                delayed_quantity_amount,
                daily_quantity_memo,
                created_at,
                updated_at
            ) values (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final TotalQuantityJpaRepository totalQuantityJpaRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long save(TotalQuantityPlan totalQuantityPlan) {
        Long totalQuantityId = saveTotalQuantity(totalQuantityPlan.totalQuantity());
        insertWeeklyQuantities(totalQuantityId, totalQuantityPlan.weeklyQuantities());
        Map<WeekPeriod, Long> weeklyQuantityIdByPeriod = loadWeeklyQuantityIdByPeriod(totalQuantityId);
        insertDailyQuantities(totalQuantityPlan.weeklyQuantities(), weeklyQuantityIdByPeriod);

        return totalQuantityId;
    }

    @Override
    public long deleteByTotalQuantityIdAndSessionalPlanIdAndUserId(Long totalQuantityId, Long sessionalPlanId, Long userId) {
        return totalQuantityJpaRepository.deleteByTotalQuantityIdAndSessionalPlanIdAndUserId(totalQuantityId, sessionalPlanId, userId);
    }

    @Override
    public boolean existsBySessionalPlanId(Long sessionalPlanId) {
        return totalQuantityJpaRepository.existsBySessionalPlan_SessionalPlanId(sessionalPlanId);
    }

    @Override
    public boolean existsBySessionalPlanIdAndSubjectName(Long sessionalPlanId, String subjectName) {
        return totalQuantityJpaRepository.existsBySessionalPlan_SessionalPlanIdAndSubject_Name(sessionalPlanId, subjectName);
    }

    @Override
    public int countBySessionalPlanId(Long sessionalPlanId) {
        return Math.toIntExact(totalQuantityJpaRepository.countBySessionalPlan_SessionalPlanId(sessionalPlanId));
    }

    private Long saveTotalQuantity(TotalQuantity totalQuantity) {
        TotalQuantityEntity savedTotalQuantity = totalQuantityJpaRepository.saveAndFlush(TotalQuantityEntity.from(totalQuantity));

        return savedTotalQuantity.getTotalQuantityId();
    }

    private void insertWeeklyQuantities(Long totalQuantityId, List<WeeklyQuantityPlan> weeklyQuantities) {
        LocalDateTime now = LocalDateTime.now();
        List<WeeklyQuantityInsertRow> rows = weeklyQuantities.stream()
                .map(weeklyQuantity -> WeeklyQuantityInsertRow.from(totalQuantityId, weeklyQuantity, now))
                .toList();

        jdbcTemplate.batchUpdate(
                WEEKLY_QUANTITY_INSERT_SQL,
                rows,
                WEEKLY_QUANTITY_INSERT_CHUNK_SIZE,
                (ps, row) -> row.bind(ps)
        );
    }

    private Map<WeekPeriod, Long> loadWeeklyQuantityIdByPeriod(Long totalQuantityId) {
        return jdbcTemplate.query("""
                select weekly_quantity_id, week_start_on, week_end_on
                from weekly_quantity
                where total_quantity_id = ?
                """,
                (rs, rowNum) -> new WeeklyQuantityIdRow(
                        rs.getLong("weekly_quantity_id"),
                        rs.getObject("week_start_on", LocalDate.class),
                        rs.getObject("week_end_on", LocalDate.class)
                ),
                totalQuantityId
        ).stream().collect(Collectors.toMap(
                row -> new WeekPeriod(row.weekStartOn(), row.weekEndOn()),
                WeeklyQuantityIdRow::weeklyQuantityId
        ));
    }

    private void insertDailyQuantities(
            List<WeeklyQuantityPlan> weeklyQuantities,
            Map<WeekPeriod, Long> weeklyQuantityIdByPeriod
    ) {
        LocalDateTime now = LocalDateTime.now();
        List<DailyQuantityInsertRow> rows = weeklyQuantities.stream()
                .flatMap(weeklyQuantity -> weeklyQuantity.dailyQuantities()
                        .stream()
                        .map(dailyQuantity -> DailyQuantityInsertRow.from(
                                weeklyQuantityIdByPeriod,
                                weeklyQuantity,
                                dailyQuantity,
                                now
                        ))
                )
                .toList();

        jdbcTemplate.batchUpdate(
                DAILY_QUANTITY_INSERT_SQL,
                rows,
                DAILY_QUANTITY_INSERT_CHUNK_SIZE,
                (ps, row) -> row.bind(ps)
        );
    }

    private record WeeklyQuantityInsertRow(
            Long totalQuantityId,
            LocalDate weekStartOn,
            LocalDate weekEndOn,
            int quantityAmount,
            String quantityMemo,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        private static WeeklyQuantityInsertRow from(
                Long totalQuantityId,
                WeeklyQuantityPlan weeklyQuantity,
                LocalDateTime now
        ) {
            return new WeeklyQuantityInsertRow(
                    totalQuantityId,
                    weeklyQuantity.weeklyQuantity().getWeekPeriod().startOn(),
                    weeklyQuantity.weeklyQuantity().getWeekPeriod().endOn(),
                    weeklyQuantity.weeklyQuantity().getWeeklyQuantityAmount().quantityAmount().value(),
                    weeklyQuantity.weeklyQuantity().getWeeklyQuantityAmount().quantityMemo().value(),
                    now,
                    now
            );
        }

        private void bind(PreparedStatement ps) throws SQLException {
            ps.setLong(1, totalQuantityId);
            ps.setObject(2, weekStartOn);
            ps.setObject(3, weekEndOn);
            ps.setInt(4, quantityAmount);
            ps.setString(5, quantityMemo);
            ps.setObject(6, createdAt);
            ps.setObject(7, updatedAt);
        }
    }

    private record WeeklyQuantityIdRow(
            Long weeklyQuantityId,
            LocalDate weekStartOn,
            LocalDate weekEndOn
    ) {
    }

    private record DailyQuantityInsertRow(
            Long weeklyQuantityId,
            LocalDate date,
            int quantityAmount,
            int completedQuantityAmount,
            int delayedQuantityAmount,
            String quantityMemo,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        private static DailyQuantityInsertRow from(
                Map<WeekPeriod, Long> weeklyQuantityIdByPeriod,
                WeeklyQuantityPlan weeklyQuantity,
                DailyQuantity dailyQuantity,
                LocalDateTime now
        ) {
            Long weeklyQuantityId = Objects.requireNonNull(
                    weeklyQuantityIdByPeriod.get(weeklyQuantity.weeklyQuantity().getWeekPeriod())
            );

            return new DailyQuantityInsertRow(
                    weeklyQuantityId,
                    dailyQuantity.getDate(),
                    dailyQuantity.getDailyQuantityAmount().quantityAmount().value(),
                    dailyQuantity.getDailyQuantityAmount().completedQuantityAmount().value(),
                    dailyQuantity.getDailyQuantityAmount().delayedQuantityAmount().value(),
                    dailyQuantity.getDailyQuantityAmount().quantityMemo().value(),
                    now,
                    now
            );
        }

        private void bind(PreparedStatement ps) throws SQLException {
            ps.setLong(1, weeklyQuantityId);
            ps.setObject(2, date);
            ps.setInt(3, quantityAmount);
            ps.setInt(4, completedQuantityAmount);
            ps.setInt(5, delayedQuantityAmount);
            ps.setString(6, quantityMemo);
            ps.setObject(7, createdAt);
            ps.setObject(8, updatedAt);
        }
    }
}
