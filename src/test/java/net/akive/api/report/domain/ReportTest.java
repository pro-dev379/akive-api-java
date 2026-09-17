package net.akive.api.report.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.EMPTY_COLLECTION;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.REPORT_DATE_IN_FUTURE;
import static net.akive.api.report.domain.FeedbackType.*;
import static net.akive.api.report.domain.ReportFixture.*;
import static net.akive.api.report.domain.ReportType.WEEKLY;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.*;

class ReportTest {

    @Test
    void 저장된_리포트를_복원한다() {
        User user = createReconstructedUser(USER_ID);
        List<ReportFeedback> feedbackItems = createReportFeedbackItems();

        Report report = Report.reconstruct(
                REPORT_ID,
                user,
                REPORT_ON,
                WEEKLY,
                feedbackItems,
                REQUESTED_AT
        );

        assertThat(report.getReportId()).isEqualTo(REPORT_ID);
        assertThat(report.getUser()).isEqualTo(user);
        assertThat(report.getReportOn()).isEqualTo(REPORT_ON);
        assertThat(report.getReportType()).isEqualTo(WEEKLY);
        assertThat(report.getFeedbackItems()).containsExactlyElementsOf(feedbackItems);
    }

    @Test
    void 리포트_작성일이_미래이면_리포트를_복원할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> Report.reconstruct(
                        REPORT_ID,
                        createReconstructedUser(USER_ID),
                        REQUESTED_AT.toLocalDate().plusDays(1),
                        WEEKLY,
                        createReportFeedbackItems(),
                        REQUESTED_AT
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(REPORT_DATE_IN_FUTURE);
    }

    @Test
    void 피드백_목록이_비어있는_값이면_리포트를_복원할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> Report.reconstruct(
                        REPORT_ID,
                        createReconstructedUser(USER_ID),
                        REPORT_ON,
                        WEEKLY,
                        List.of(),
                        REQUESTED_AT
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(EMPTY_COLLECTION);
    }

    @Test
    void 리포트를_생성한다() {
        User user = createReconstructedUser(USER_ID);
        List<ReportFeedback> feedbackItems = createReportFeedbackItems();

        Report report = Report.create(
                user,
                REPORT_ON,
                WEEKLY,
                feedbackItems,
                REQUESTED_AT
        );

        assertThat(report.getReportId()).isNull();
        assertThat(report.getUser()).isEqualTo(user);
        assertThat(report.getReportOn()).isEqualTo(REPORT_ON);
        assertThat(report.getReportType()).isEqualTo(WEEKLY);
        assertThat(report.getFeedbackItems()).containsExactlyElementsOf(feedbackItems);
    }

    @Test
    void 리포트_작성일이_미래이면_리포트를_생성할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> Report.create(
                        createReconstructedUser(USER_ID),
                        REQUESTED_AT.toLocalDate().plusDays(1),
                        WEEKLY,
                        createReportFeedbackItems(),
                        REQUESTED_AT
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(REPORT_DATE_IN_FUTURE);
    }

    @Test
    void 피드백_목록이_비어있는_값이면_리포트를_생성할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> Report.create(
                        createReconstructedUser(USER_ID),
                        REPORT_ON,
                        WEEKLY,
                        List.of(),
                        REQUESTED_AT
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(EMPTY_COLLECTION);
    }

    @Test
    void 리포트_피드백_목록을_수정한다() {
        Report report = Report.create(
                createReconstructedUser(USER_ID),
                REPORT_ON,
                WEEKLY,
                createReportFeedbackItems(),
                REQUESTED_AT
        );
        ReportFeedback targetFeedback = createReportFeedback(NOTE);

        report.updateFeedbackItems(List.of(targetFeedback));

        assertThat(report.getFeedbackItems()).containsExactly(targetFeedback);
    }

    @Test
    void 리포트_피드백_목록을_비어있는_값으로_수정할_수_없다() {
        Report report = Report.create(
                createReconstructedUser(USER_ID),
                REPORT_ON,
                WEEKLY,
                createReportFeedbackItems(),
                REQUESTED_AT
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> report.updateFeedbackItems(List.of()))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(EMPTY_COLLECTION);
    }

    @Test
    void 피드백_목록은_외부에서_수정할_수_없다() {
        List<ReportFeedback> feedbackItems = new ArrayList<>(createReportFeedbackItems());
        Report report = Report.create(
                createReconstructedUser(USER_ID),
                REPORT_ON,
                WEEKLY,
                feedbackItems,
                REQUESTED_AT
        );

        feedbackItems.clear();

        assertThat(report.getFeedbackItems()).containsExactlyElementsOf(createReportFeedbackItems());
        assertThatThrownBy(() -> report.getFeedbackItems().add(createReportFeedback(HIGHLIGHT)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void 같은_식별자를_가진_리포트는_같게_취급한다() {
        User user = createReconstructedUser(USER_ID);
        Report report = Report.reconstruct(
                REPORT_ID,
                user,
                REPORT_ON,
                WEEKLY,
                List.of(createReportFeedback(HIGHLIGHT)),
                REQUESTED_AT
        );
        Report sameReport = Report.reconstruct(
                REPORT_ID,
                user,
                REPORT_ON.plusDays(1),
                WEEKLY,
                List.of(createReportFeedback(IMPROVEMENT)),
                REQUESTED_AT.plusDays(1)
        );

        assertThat(report).isEqualTo(sameReport);
        assertThat(report.hashCode()).isEqualTo(sameReport.hashCode());
    }

    @Test
    void 식별자가_다른_리포트는_다르게_취급한다() {
        Report report = Report.reconstruct(
                REPORT_ID,
                createReconstructedUser(USER_ID),
                REPORT_ON,
                WEEKLY,
                createReportFeedbackItems(),
                REQUESTED_AT
        );
        Report anotherReport = Report.reconstruct(
                ANOTHER_REPORT_ID,
                createReconstructedUser(USER_ID),
                REPORT_ON,
                WEEKLY,
                createReportFeedbackItems(),
                REQUESTED_AT
        );

        assertThat(report).isNotEqualTo(anotherReport);
    }

    @Test
    void 식별자가_없는_리포트끼리는_나머지_값이_같더라도_다른_것으로_취급한다() {
        User user = createReconstructedUser(USER_ID);
        Report report = Report.create(user, REPORT_ON, WEEKLY, createReportFeedbackItems(), REQUESTED_AT);
        Report anotherReport = Report.create(user, REPORT_ON, WEEKLY, createReportFeedbackItems(), REQUESTED_AT);

        assertThat(report).isNotEqualTo(anotherReport);
    }
}
