package net.akive.api.dailylog.adapter.webapi.fixture;

import net.akive.api.dailylog.adapter.webapi.dto.request.DailyLogCreateRequest;
import net.akive.api.dailylog.adapter.webapi.dto.request.DailyLogUpdateRequest;
import net.akive.api.dailylog.adapter.webapi.dto.request.FocusTimeRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static net.akive.api.dailylog.domain.DailyLogType.STUDY;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;

public class DailyLogRequestFixture {

    private DailyLogRequestFixture() {
    }

    public static DailyLogCreateRequest createDailyLogCreateRequest() {
        return createDailyLogCreateRequest(createFocusTimeRequest(), FOCUS_TIME_SEC);
    }

    public static DailyLogCreateRequest createDailyLogCreateRequest(FocusTimeRequest focusTimeRequest) {
        return createDailyLogCreateRequest(
                focusTimeRequest,
                Duration.between(focusTimeRequest.startOn(), focusTimeRequest.endOn()).toSeconds()
        );
    }

    public static DailyLogCreateRequest createDailyLogCreateRequest(
            FocusTimeRequest focusTimeRequest,
            Long focusTimeSec
    ) {
        return new DailyLogCreateRequest(
                new FocusTimeRequest(
                        focusTimeRequest.startOn(),
                        focusTimeRequest.endOn(),
                        focusTimeSec,
                        focusTimeRequest.focusRateExcluded()
                ),
                STUDY,
                CONTENTS
        );
    }

    public static FocusTimeRequest createFocusTimeRequest() {
        return createFocusTimeRequest(START_ON, END_ON);
    }

    public static DailyLogUpdateRequest createDailyLogUpdateRequest() {
        return createDailyLogUpdateRequest(createFocusTimeRequest(TARGET_START_ON, TARGET_END_ON));
    }

    public static DailyLogUpdateRequest createDailyLogUpdateRequest(FocusTimeRequest targetFocusTime) {
        return new DailyLogUpdateRequest(targetFocusTime, STUDY, TARGET_CONTENTS);
    }

    public static DailyLogUpdateRequest createDailyLogUpdateRequest(
            FocusTimeRequest targetFocusTime,
            Long focusTimeSec
    ) {
        return createDailyLogUpdateRequest(
                new FocusTimeRequest(
                        targetFocusTime.startOn(),
                        targetFocusTime.endOn(),
                        focusTimeSec,
                        targetFocusTime.focusRateExcluded()
                )
        );
    }

    public static FocusTimeRequest createFocusTimeRequest(LocalDateTime startOn, LocalDateTime endOn) {
        return new FocusTimeRequest(startOn, endOn, FOCUS_TIME_SEC, false);
    }

    public static Stream<DailyLogCreateRequest> invalidCreateRequests() {
        return Stream.of(
                new DailyLogCreateRequest(
                        null,
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        new FocusTimeRequest(null, END_ON, FOCUS_TIME_SEC, false),
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        new FocusTimeRequest(START_ON, null, FOCUS_TIME_SEC, false),
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        createFocusTimeRequest(),
                        null,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        new FocusTimeRequest(START_ON, END_ON, -1L, false),
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(createFocusTimeRequest(), STUDY, " "),
                new DailyLogCreateRequest(createFocusTimeRequest(), STUDY, null)
        );
    }

    public static Stream<DailyLogUpdateRequest> invalidUpdateRequests() {
        return Stream.of(
                new DailyLogUpdateRequest(
                        null,
                        STUDY,
                        TARGET_CONTENTS
                ),
                new DailyLogUpdateRequest(
                        new FocusTimeRequest(null, TARGET_END_ON, FOCUS_TIME_SEC, false),
                        STUDY,
                        TARGET_CONTENTS
                ),
                new DailyLogUpdateRequest(
                        new FocusTimeRequest(TARGET_START_ON, null, FOCUS_TIME_SEC, false),
                        STUDY,
                        TARGET_CONTENTS
                ),
                new DailyLogUpdateRequest(
                        createFocusTimeRequest(TARGET_START_ON, TARGET_END_ON),
                        null,
                        TARGET_CONTENTS
                ),
                new DailyLogUpdateRequest(
                        new FocusTimeRequest(TARGET_START_ON, TARGET_END_ON, -1L, false),
                        STUDY,
                        TARGET_CONTENTS
                ),
                new DailyLogUpdateRequest(createFocusTimeRequest(), STUDY, " "),
                new DailyLogUpdateRequest(createFocusTimeRequest(), STUDY, null)
        );
    }
}
