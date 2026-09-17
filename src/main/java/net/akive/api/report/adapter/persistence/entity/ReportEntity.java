package net.akive.api.report.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportFeedback;
import net.akive.api.report.domain.ReportType;
import net.akive.api.user.adapter.persistence.entity.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
        name = "report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-report-user_id-report_on-report_type",
                        columnNames = {"user_id", "report_on", "report_type"}
                )
        }
)
@Getter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class ReportEntity extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long reportId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private LocalDate reportOn;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private ReportType reportType;

    @OrderBy("sortOrder ASC")
    @OneToMany(mappedBy = "report", cascade = ALL, orphanRemoval = true)
    private List<ReportFeedbackEntity> feedbackItems = new ArrayList<>();

    public static ReportEntity from(Report report) {
        ReportEntity reportEntity = new ReportEntity(
                report.getReportId(),
                UserEntity.from(report.getUser()),
                report.getReportOn(),
                report.getReportType(),
                new ArrayList<>()
        );

        report.getFeedbackItems()
                .forEach(reportFeedback -> reportEntity.addReportFeedback(
                        ReportFeedbackEntity.from(reportFeedback)
                ));

        return reportEntity;
    }

    public Report to(LocalDateTime reconstructedAt) {
        return Report.reconstruct(
                this.reportId,
                this.user.to(),
                this.reportOn,
                this.reportType,
                this.feedbackItems.stream().map(ReportFeedbackEntity::to).collect(Collectors.toList()),
                reconstructedAt
        );
    }

    private void addReportFeedback(ReportFeedbackEntity reportFeedback) {
        reportFeedback.assignReport(this);

        this.feedbackItems.add(reportFeedback);
    }

    public void clearFeedbackItems() {
        this.feedbackItems.clear();
    }

    public void updateFeedbackItems(List<ReportFeedback> feedbackItems) {
        feedbackItems.stream()
                .map(ReportFeedbackEntity::from)
                .forEach(this::addReportFeedback);
    }
}
