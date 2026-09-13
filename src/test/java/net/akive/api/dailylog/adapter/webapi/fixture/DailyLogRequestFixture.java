package net.akive.api.dailylog.adapter.webapi.fixture;

import net.akive.api.dailylog.adapter.webapi.dto.request.DailyLogCreateRequest;
import net.akive.api.dailylog.adapter.webapi.dto.request.DailyLogTimeRangeRequest;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static net.akive.api.dailylog.domain.DailyLogType.STUDY;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;

public class DailyLogRequestFixture {

    public static DailyLogCreateRequest createDailyLogCreateRequest() {
        return createDailyLogCreateRequest(createDailyLogTimeRangeRequest(), FOCUS_TIME_SEC);
    }

    public static DailyLogCreateRequest createDailyLogCreateRequest(
            DailyLogTimeRangeRequest timeRangeRequest,
            Long focusTimeSec
    ) {
        return new DailyLogCreateRequest(
                timeRangeRequest,
                STUDY,
                focusTimeSec,
                false,
                CONTENTS
        );
    }

    public static DailyLogTimeRangeRequest createDailyLogTimeRangeRequest() {
        return createDailyLogTimeRangeRequest(START_ON, END_ON);
    }

    public static DailyLogTimeRangeRequest createDailyLogTimeRangeRequest(
            LocalDateTime starOn,
            LocalDateTime endOn
    ) {
        return new DailyLogTimeRangeRequest(starOn, endOn);
    }

    public static Stream<DailyLogCreateRequest> invalidCreateRequests() {
        return Stream.of(
                new DailyLogCreateRequest(null, STUDY, FOCUS_TIME_SEC, false, CONTENTS),
                new DailyLogCreateRequest(
                        new DailyLogTimeRangeRequest(null, END_ON),
                        STUDY,
                        FOCUS_TIME_SEC,
                        false,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        new DailyLogTimeRangeRequest(START_ON, null),
                        STUDY,
                        FOCUS_TIME_SEC,
                        false,
                        CONTENTS
                ),
                new DailyLogCreateRequest(createDailyLogTimeRangeRequest(), null, FOCUS_TIME_SEC, false, CONTENTS),
                new DailyLogCreateRequest(createDailyLogTimeRangeRequest(), STUDY, -1L, false, CONTENTS),
                new DailyLogCreateRequest(createDailyLogTimeRangeRequest(), STUDY, FOCUS_TIME_SEC, false, " "),
                new DailyLogCreateRequest(createDailyLogTimeRangeRequest(), STUDY, FOCUS_TIME_SEC, false, null)
        );
    }
}
