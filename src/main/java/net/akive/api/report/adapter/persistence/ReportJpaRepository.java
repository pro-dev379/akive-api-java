package net.akive.api.report.adapter.persistence;

import net.akive.api.report.adapter.persistence.entity.ReportEntity;
import net.akive.api.report.application.provided.dto.ReportHeaderResult;
import net.akive.api.report.domain.ReportType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

interface ReportJpaRepository extends JpaRepository<ReportEntity, Long> {

    @Override
    @NotNull
    @EntityGraph(attributePaths = {"user", "feedbackItems"})
    Optional<ReportEntity> findById(@NotNull Long reportId);

    @EntityGraph(attributePaths = {"user", "feedbackItems"})
    Optional<ReportEntity> findByReportIdAndUser_UserId(Long reportId, Long userId);

    @EntityGraph(attributePaths = "user")
    List<ReportEntity> findAllByUser_UserIdAndReportTypeAndReportOnBetweenOrderByReportOnAsc(
            Long userId,
            ReportType reportType,
            LocalDate startOn,
            LocalDate endOn
    );

    @Query("""
        select new net.akive.api.report.application.provided.dto.ReportHeaderResult(
            r.reportId,
            r.reportOn,
            r.reportType
        )
        from ReportEntity r
        where r.user.userId = :userId
    """)
    Page<ReportHeaderResult> findHeaderPageByUserId(Long userId, Pageable pageable);

    @Query("""
        select new net.akive.api.report.application.provided.dto.ReportHeaderResult(
            r.reportId,
            r.reportOn,
            r.reportType
        )
        from ReportEntity r
        where r.user.userId = :userId
            and r.reportOn >= :startOn and r.reportOn <= :endOn
        order by r.reportOn asc
    """)
    List<ReportHeaderResult> findHeadersByUserIdAndReportOnBetweenOrderByReportOnAsc(
            Long userId,
            LocalDate startOn,
            LocalDate endOn
    );

    boolean existsByUser_UserIdAndReportOnAndReportType(Long userId, LocalDate reportOn, ReportType reportType);
}
