package net.akive.api.report.application.service;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanFinder;
import net.akive.api.report.domain.ReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.report.domain.ReportFixture.REPORT_ON;
import static net.akive.api.report.domain.ReportFixture.createYear2026SessionPeriod;
import static net.akive.api.report.domain.ReportType.MONTHLY;
import static net.akive.api.report.domain.ReportType.WEEKLY;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ReportQueryServiceTest {

    @Mock
    SessionalPlanFinder sessionalPlanFinder;

    @InjectMocks
    ReportQueryService reportQueryService;

    @Test
    void 작성_가능한_리포트_타입을_조회한다() {
        given(sessionalPlanFinder.findSessionPeriodsByUserId(USER_ID))
                .willReturn(List.of(createYear2026SessionPeriod()));

        List<ReportType> reportTypes = reportQueryService.findAvailableReportTypes(USER_ID, REPORT_ON);

        assertThat(reportTypes).containsExactly(WEEKLY, MONTHLY);

        then(sessionalPlanFinder).should().findSessionPeriodsByUserId(USER_ID);
    }

    @Test
    void 리포트_날짜와_관련된_세셔널_플랜이_없으면_작성_가능한_리포트_타입_조회에_실패한다() {
        given(sessionalPlanFinder.findSessionPeriodsByUserId(USER_ID))
                .willReturn(List.of(createYear2026SessionPeriod()));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> reportQueryService.findAvailableReportTypes(USER_ID, LocalDate.of(2024, 1, 1)))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanFinder).should().findSessionPeriodsByUserId(USER_ID);
    }
}
