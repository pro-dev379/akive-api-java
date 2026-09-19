package net.akive.api.report.application.service;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanFinder;
import net.akive.api.report.application.required.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_REPORT_TYPE;
import static net.akive.api.common.exception.business.ResourceConflictError.REPORT_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.report.domain.ReportFixture.REPORT_ON;
import static net.akive.api.report.domain.ReportFixture.createYear2026SessionPeriod;
import static net.akive.api.report.domain.ReportType.DAILY;
import static net.akive.api.report.domain.ReportType.WEEKLY;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReportValidationServiceTest {

    @Mock
    SessionalPlanFinder sessionalPlanFinder;

    @Mock
    ReportRepository reportRepository;

    @InjectMocks
    ReportValidationService reportValidationService;

    @Test
    void 리포트_생성_가능_여부를_검증한다() {
        given(sessionalPlanFinder.findSessionPeriodsByUserId(USER_ID))
                .willReturn(List.of(createYear2026SessionPeriod()));
        given(reportRepository.existsByUserIdAndReportOnAndReportType(USER_ID, REPORT_ON, WEEKLY))
                .willReturn(false);

        reportValidationService.validateCreatable(USER_ID, REPORT_ON, WEEKLY);

        then(sessionalPlanFinder).should().findSessionPeriodsByUserId(USER_ID);
        then(reportRepository).should().existsByUserIdAndReportOnAndReportType(USER_ID, REPORT_ON, WEEKLY);
    }

    @Test
    void 리포트_작성일에_해당하는_세션_기간이_없으면_생성_가능_여부_검증에_실패한다() {
        LocalDate reportOn = LocalDate.of(2024, 1, 1);
        given(sessionalPlanFinder.findSessionPeriodsByUserId(USER_ID))
                .willReturn(List.of(createYear2026SessionPeriod()));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> reportValidationService.validateCreatable(USER_ID, reportOn, DAILY))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanFinder).should().findSessionPeriodsByUserId(USER_ID);
        then(reportRepository).should(never()).existsByUserIdAndReportOnAndReportType(USER_ID, reportOn, DAILY);
    }

    @Test
    void 리포트_작성_가능한_리포트_타입이_아니면_생성_가능_여부_검증에_실패한다() {
        given(sessionalPlanFinder.findSessionPeriodsByUserId(USER_ID))
                .willReturn(List.of(createYear2026SessionPeriod()));

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> reportValidationService.validateCreatable(USER_ID, REPORT_ON, DAILY))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_REPORT_TYPE);

        then(sessionalPlanFinder).should().findSessionPeriodsByUserId(USER_ID);
        then(reportRepository).should(never()).existsByUserIdAndReportOnAndReportType(USER_ID, REPORT_ON, DAILY);
    }

    @Test
    void 이미_리포트_작성일과_리포트_타입이_같은_리포트가_존재하면_생성_가능_여부_검증에_실패한다() {
        given(sessionalPlanFinder.findSessionPeriodsByUserId(USER_ID))
                .willReturn(List.of(createYear2026SessionPeriod()));
        given(reportRepository.existsByUserIdAndReportOnAndReportType(USER_ID, REPORT_ON, WEEKLY))
                .willReturn(true);

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> reportValidationService.validateCreatable(USER_ID, REPORT_ON, WEEKLY))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(REPORT_ALREADY_EXISTS);

        then(sessionalPlanFinder).should().findSessionPeriodsByUserId(USER_ID);
        then(reportRepository).should().existsByUserIdAndReportOnAndReportType(USER_ID, REPORT_ON, WEEKLY);
    }
}
