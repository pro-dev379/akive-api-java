package net.akive.api.report.adapter.webapi;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.report.application.provided.ReportFinder;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.List;

import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.report.domain.ReportFixture.REPORT_ON;
import static net.akive.api.report.domain.ReportType.MONTHLY;
import static net.akive.api.report.domain.ReportType.WEEKLY;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@WebMvcTest(ReportApi.class)
class ReportApiTest extends PrivateApiTestSupport {

    private static final String AVAILABLE_REPORT_TYPES_URL = "/reports/available-types?reportOn={reportOn}";

    @MockitoBean
    ReportFinder reportFinder;

    @Test
    void 작성_가능한_리포트_타입을_조회한다() {
        given(reportFinder.findAvailableReportTypes(USER_ID, REPORT_ON))
                .willReturn(List.of(WEEKLY, MONTHLY));

        MvcTestResultAssert result = get(
                AVAILABLE_REPORT_TYPES_URL,
                USER_ID,
                REPORT_ON
        ).hasStatusOk();

        result.bodyJson()
                .extractingPath("$.reportTypes[0].code")
                .isEqualTo(WEEKLY.name());
        result.bodyJson()
                .extractingPath("$.reportTypes[0].label")
                .isEqualTo(WEEKLY.getLabel());
        result.bodyJson()
                .extractingPath("$.reportTypes[1].code")
                .isEqualTo(MONTHLY.name());
        result.bodyJson()
                .extractingPath("$.reportTypes[1].label")
                .isEqualTo(MONTHLY.getLabel());

        then(reportFinder).should().findAvailableReportTypes(USER_ID, REPORT_ON);
    }

    @Test
    void 작성_가능한_리포트_타입_조회_시_세셔널_플랜이_없으면_404를_응답한다() {
        given(reportFinder.findAvailableReportTypes(USER_ID, REPORT_ON))
                .willThrow(new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND));

        MvcTestResultAssert result = get(
                AVAILABLE_REPORT_TYPES_URL,
                USER_ID,
                REPORT_ON
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, SESSIONAL_PLAN_NOT_FOUND);

        then(reportFinder).should().findAvailableReportTypes(USER_ID, REPORT_ON);
    }
}
