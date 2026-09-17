package net.akive.api.report.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.user.domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.REPORT_DATE_IN_FUTURE;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class Report {

    private Long reportId;

    private User user;

    private LocalDate reportOn;

    private ReportType reportType;

    private List<ReportFeedback> feedbackItems;

    public static Report reconstruct(
            Long reportId,
            User user,
            LocalDate reportOn,
            ReportType reportType,
            List<ReportFeedback> feedbackItems,
            LocalDateTime requestedAt
    ) {
        validateReportOnNotFuture(reportOn, requestedAt);

        return Report.builder()
                .reportId(requirePositive(reportId))
                .user(requireNonNull(user))
                .reportOn(requireNonNull(reportOn))
                .reportType(requireNonNull(reportType))
                .feedbackItems(List.copyOf(requireNotEmpty(feedbackItems)))
                .build();
    }

    public static Report create(
            User user,
            LocalDate reportOn,
            ReportType reportType,
            List<ReportFeedback> feedbackItems,
            LocalDateTime requestedAt
    ) {
        validateReportOnNotFuture(reportOn, requestedAt);

        return Report.builder()
                .user(requireNonNull(user))
                .reportOn(requireNonNull(reportOn))
                .reportType(requireNonNull(reportType))
                .feedbackItems(List.copyOf(requireNotEmpty(feedbackItems)))
                .build();
    }

    public void updateFeedbackItems(List<ReportFeedback> targetFeedbackItems) {
        this.feedbackItems = List.copyOf(requireNotEmpty(targetFeedbackItems));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Report that = (Report) o;

        return this.reportId != null && Objects.equals(this.reportId, that.reportId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private static void validateReportOnNotFuture(LocalDate reportOn, LocalDateTime baseTime) {
        requireNonNull(reportOn);
        requireNonNull(baseTime);

        if (reportOn.isAfter(baseTime.toLocalDate())) {
            throw new BusinessRuleViolationException(REPORT_DATE_IN_FUTURE);
        }
    }
}
