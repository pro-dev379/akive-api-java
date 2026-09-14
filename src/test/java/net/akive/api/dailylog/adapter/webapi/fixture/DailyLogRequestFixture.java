package net.akive.api.dailylog.adapter.webapi.fixture;

import net.akive.api.dailylog.adapter.webapi.dto.request.DailyLogCreateRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static net.akive.api.dailylog.domain.DailyLogType.STUDY;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;

public class DailyLogRequestFixture {

    public static DailyLogCreateRequest createDailyLogCreateRequest() {
        return createDailyLogCreateRequest(createFocusTimeRequest(), FOCUS_TIME_SEC);
    }

    public static DailyLogCreateRequest createDailyLogCreateRequest(
            DailyLogCreateRequest.FocusTimeRequest focusTimeRequest
    ) {
        return createDailyLogCreateRequest(
                focusTimeRequest,
                Duration.between(focusTimeRequest.startOn(), focusTimeRequest.endOn()).toSeconds()
        );
    }

    public static DailyLogCreateRequest createDailyLogCreateRequest(
            DailyLogCreateRequest.FocusTimeRequest focusTimeRequest,
            Long focusTimeSec
    ) {
        return new DailyLogCreateRequest(
                new DailyLogCreateRequest.FocusTimeRequest(
                        focusTimeRequest.logOn(),
                        focusTimeRequest.startOn(),
                        focusTimeRequest.endOn(),
                        focusTimeSec,
                        focusTimeRequest.focusRateExcluded()
                ),
                STUDY,
                CONTENTS
        );
    }

    public static DailyLogCreateRequest.FocusTimeRequest createFocusTimeRequest() {
        return createFocusTimeRequest(LOG_ON, START_ON, END_ON);
    }

    public static DailyLogCreateRequest.FocusTimeRequest createFocusTimeRequest(LocalDate logOn) {
        return createFocusTimeRequest(logOn, START_ON, END_ON);
    }

    public static DailyLogCreateRequest.FocusTimeRequest createFocusTimeRequest(
            LocalDateTime startOn,
            LocalDateTime endOn
    ) {
        return createFocusTimeRequest(LOG_ON, startOn, endOn);
    }

    public static DailyLogCreateRequest.FocusTimeRequest createFocusTimeRequest(
            LocalDate logOn,
            LocalDateTime startOn,
            LocalDateTime endOn
    ) {
        return new DailyLogCreateRequest.FocusTimeRequest(logOn, startOn, endOn, FOCUS_TIME_SEC, false);
    }

    public static Stream<DailyLogCreateRequest> invalidCreateRequests() {
        return Stream.of(
                new DailyLogCreateRequest(null, STUDY, CONTENTS),
                new DailyLogCreateRequest(
                        new DailyLogCreateRequest.FocusTimeRequest(null, START_ON, END_ON, FOCUS_TIME_SEC, false),
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        new DailyLogCreateRequest.FocusTimeRequest(LOG_ON, null, END_ON, FOCUS_TIME_SEC, false),
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        new DailyLogCreateRequest.FocusTimeRequest(LOG_ON, START_ON, null, FOCUS_TIME_SEC, false),
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(createFocusTimeRequest(), null, CONTENTS),
                new DailyLogCreateRequest(
                        new DailyLogCreateRequest.FocusTimeRequest(LOG_ON, START_ON, END_ON, -1L, false),
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(createFocusTimeRequest(), STUDY, " "),
                new DailyLogCreateRequest(createFocusTimeRequest(), STUDY, null)
        );
    }
}
