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
                LOG_ON,
                new DailyLogCreateRequest.FocusTimeRequest(
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
        return createFocusTimeRequest(START_ON, END_ON);
    }

    public static DailyLogCreateRequest createDailyLogCreateRequest(LocalDate logOn) {
        return new DailyLogCreateRequest(logOn, createFocusTimeRequest(), STUDY, CONTENTS);
    }

    public static DailyLogCreateRequest.FocusTimeRequest createFocusTimeRequest(
            LocalDateTime startOn,
            LocalDateTime endOn
    ) {
        return new DailyLogCreateRequest.FocusTimeRequest(startOn, endOn, FOCUS_TIME_SEC, false);
    }

    public static Stream<DailyLogCreateRequest> invalidCreateRequests() {
        return Stream.of(
                new DailyLogCreateRequest(
                        null,
                        createFocusTimeRequest(),
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        LOG_ON,
                        null,
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        LOG_ON,
                        new DailyLogCreateRequest.FocusTimeRequest(null, END_ON, FOCUS_TIME_SEC, false),
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        LOG_ON,
                        new DailyLogCreateRequest.FocusTimeRequest(START_ON, null, FOCUS_TIME_SEC, false),
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        LOG_ON,
                        createFocusTimeRequest(),
                        null,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        LOG_ON,
                        new DailyLogCreateRequest.FocusTimeRequest(START_ON, END_ON, -1L, false),
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(LOG_ON, createFocusTimeRequest(), STUDY, " "),
                new DailyLogCreateRequest(LOG_ON, createFocusTimeRequest(), STUDY, null)
        );
    }
}
