package net.akive.api.plan.adapter.webapi.sessionalplan.fixture;

import net.akive.api.plan.adapter.webapi.common.dto.request.SessionPeriodRequest;
import net.akive.api.plan.adapter.webapi.sessionalplan.dto.request.SessionalPlanCreateRequest;
import net.akive.api.plan.adapter.webapi.sessionalplan.dto.request.SessionalPlanUpdateRequest;

import java.time.LocalDate;
import java.util.stream.Stream;

import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.SESSION_NAME;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.SESSION_PERIOD;
import static net.akive.api.plan.domain.policy.PlanPolicy.SESSION_NAME_MAX_LENGTH;

public class SessionalPlanRequestFixture {

    private SessionalPlanRequestFixture() {
    }

    public static SessionalPlanCreateRequest createSessionalPlanCreateRequest() {
        return createSessionalPlanCreateRequest(SESSION_NAME);
    }

    public static SessionalPlanCreateRequest createSessionalPlanCreateRequest(String sessionName) {
        return new SessionalPlanCreateRequest(sessionName, createSessionPeriodRequest());
    }

    public static SessionalPlanUpdateRequest createSessionalPlanUpdateRequest() {
        return createSessionalPlanUpdateRequest(createSessionPeriodRequest());
    }

    public static SessionalPlanUpdateRequest createSessionalPlanUpdateRequest(
            SessionPeriodRequest targetSessionPeriod
    ) {
        return new SessionalPlanUpdateRequest(SESSION_NAME, targetSessionPeriod);
    }

    public static SessionPeriodRequest createSessionPeriodRequest() {
        return new SessionPeriodRequest(SESSION_PERIOD.startOn(), SESSION_PERIOD.endOn());
    }

    public static Stream<SessionalPlanCreateRequest> invalidCreateRequests() {
        return invalidRequestValues()
                .map(value -> new SessionalPlanCreateRequest(value.sessionName(), value.sessionPeriod()));
    }

    public static Stream<SessionalPlanUpdateRequest> invalidUpdateRequests() {
        return invalidRequestValues()
                .map(value -> new SessionalPlanUpdateRequest(value.sessionName(), value.sessionPeriod()));
    }

    public static String createInvalidDateFormatRequestJson() {
        return """
                {
                  "sessionName": "%s",
                  "sessionPeriod": {
                    "startOn": "2026/01/01",
                    "endOn": "2026-02-01"
                  }
                }
                """.formatted(SESSION_NAME);
    }

    public static String updateInvalidDateFormatRequestJson() {
        return """
                {
                  "targetSessionName": "%s",
                  "targetSessionPeriod": {
                    "startOn": "2026/01/01",
                    "endOn": "2026-02-01"
                  }
                }
                """.formatted(SESSION_NAME);
    }

    private static Stream<SessionalPlanRequestValue> invalidRequestValues() {
        return Stream.of(
                new SessionalPlanRequestValue(null, createSessionPeriodRequest()),
                new SessionalPlanRequestValue("", createSessionPeriodRequest()),
                new SessionalPlanRequestValue(
                        "a".repeat(SESSION_NAME_MAX_LENGTH + 1),
                        createSessionPeriodRequest()
                ),
                new SessionalPlanRequestValue(SESSION_NAME, null),
                new SessionalPlanRequestValue(
                        SESSION_NAME,
                        new SessionPeriodRequest(null, LocalDate.of(2026, 1, 12))
                ),
                new SessionalPlanRequestValue(
                        SESSION_NAME,
                        new SessionPeriodRequest(LocalDate.of(2026, 1, 1), null)
                ),
                new SessionalPlanRequestValue(
                        SESSION_NAME,
                        new SessionPeriodRequest(
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 1, 1)
                        )
                ),
                new SessionalPlanRequestValue(
                        SESSION_NAME,
                        new SessionPeriodRequest(
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2999, 1, 1)
                        )
                )
        );
    }

    private record SessionalPlanRequestValue(
            String sessionName,
            SessionPeriodRequest sessionPeriod
    ) {
    }
}
