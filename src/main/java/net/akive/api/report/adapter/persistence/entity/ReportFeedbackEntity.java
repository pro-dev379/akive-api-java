package net.akive.api.report.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.report.domain.FeedbackSource;
import net.akive.api.report.domain.FeedbackType;
import net.akive.api.report.domain.ReportFeedback;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.report.domain.FeedbackSource.USER;

@Entity
@Table(
        name = "report_feedback",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-report_feedback-report_id-feedback_type-sort_order",
                        columnNames = {"report_id", "feedback_type", "sort_order"}
                )
        }
)
@Getter
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class ReportFeedbackEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long reportFeedbackId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private ReportEntity report;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private FeedbackType feedbackType;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'USER'")
    private FeedbackSource feedbackSource = USER;

    @Column(name = "sort_order", nullable = false, columnDefinition = "INT UNSIGNED")
    private long sortOrder;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ReportFeedbackEntity from(ReportFeedback reportFeedback) {
        ReportFeedbackEntity reportFeedbackEntity = new ReportFeedbackEntity();
        reportFeedbackEntity.feedbackType = reportFeedback.feedbackType();
        reportFeedbackEntity.feedbackSource = reportFeedback.feedbackSource();
        reportFeedbackEntity.sortOrder = reportFeedback.sortOrder();
        reportFeedbackEntity.content = reportFeedback.content();

        return reportFeedbackEntity;
    }

    public ReportFeedback to() {
        return new ReportFeedback(
                this.feedbackType,
                this.feedbackSource,
                this.sortOrder,
                this.content
        );
    }

    public void assignReport(ReportEntity reportEntity) {
        this.report = reportEntity;
    }
}
