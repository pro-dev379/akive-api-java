package net.akive.api.dailylog.adapter.webapi.fixture;

import net.akive.api.dailylog.adapter.webapi.dto.request.DailyLogCreateRequest;
import net.akive.api.dailylog.adapter.webapi.dto.request.DailyLogUpdateRequest;
import net.akive.api.dailylog.adapter.webapi.dto.request.FocusTimeRequest;

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
            FocusTimeRequest focusTimeRequest
    ) {
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
                LOG_ON,
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

    public static DailyLogCreateRequest createDailyLogCreateRequest(LocalDate logOn) {
        return new DailyLogCreateRequest(logOn, createFocusTimeRequest(), STUDY, CONTENTS);
    }

    public static DailyLogUpdateRequest createDailyLogUpdateRequest() {
        return createDailyLogUpdateRequest(TARGET_LOG_ON, createFocusTimeRequest(TARGET_START_ON, TARGET_END_ON));
    }

    public static DailyLogUpdateRequest createDailyLogUpdateRequest(LocalDate targetLogOn) {
        return createDailyLogUpdateRequest(targetLogOn, createFocusTimeRequest(TARGET_START_ON, TARGET_END_ON));
    }

    public static DailyLogUpdateRequest createDailyLogUpdateRequest(FocusTimeRequest targetFocusTime) {
        return createDailyLogUpdateRequest(TARGET_LOG_ON, targetFocusTime);
    }

    public static DailyLogUpdateRequest createDailyLogUpdateRequest(
            FocusTimeRequest targetFocusTime,
            Long focusTimeSec
    ) {
        return createDailyLogUpdateRequest(
                TARGET_LOG_ON,
                new FocusTimeRequest(
                        targetFocusTime.startOn(),
                        targetFocusTime.endOn(),
                        focusTimeSec,
                        targetFocusTime.focusRateExcluded()
                )
        );
    }

    public static DailyLogUpdateRequest createDailyLogUpdateRequest(
            LocalDate targetLogOn,
            FocusTimeRequest targetFocusTime
    ) {
        return new DailyLogUpdateRequest(targetLogOn, targetFocusTime, STUDY, TARGET_CONTENTS);
    }

    public static FocusTimeRequest createFocusTimeRequest(
            LocalDateTime startOn,
            LocalDateTime endOn
    ) {
        return new FocusTimeRequest(startOn, endOn, FOCUS_TIME_SEC, false);
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
                        new FocusTimeRequest(null, END_ON, FOCUS_TIME_SEC, false),
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        LOG_ON,
                        new FocusTimeRequest(START_ON, null, FOCUS_TIME_SEC, false),
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
                        new FocusTimeRequest(START_ON, END_ON, -1L, false),
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(LOG_ON, createFocusTimeRequest(), STUDY, " "),
                new DailyLogCreateRequest(LOG_ON, createFocusTimeRequest(), STUDY, null)
        );
    }

    public static Stream<DailyLogUpdateRequest> invalidUpdateRequests() {
        return Stream.of(
                new DailyLogUpdateRequest(
                        null,
                        createFocusTimeRequest(),
                        STUDY,
                        TARGET_CONTENTS
                ),
                new DailyLogUpdateRequest(
                        TARGET_LOG_ON,
                        null,
                        STUDY,
                        TARGET_CONTENTS
                ),
                new DailyLogUpdateRequest(
                        TARGET_LOG_ON,
                        new FocusTimeRequest(null, TARGET_END_ON, FOCUS_TIME_SEC, false),
                        STUDY,
                        TARGET_CONTENTS
                ),
                new DailyLogUpdateRequest(
                        TARGET_LOG_ON,
                        new FocusTimeRequest(TARGET_START_ON, null, FOCUS_TIME_SEC, false),
                        STUDY,
                        TARGET_CONTENTS
                ),
                new DailyLogUpdateRequest(
                        TARGET_LOG_ON,
                        createFocusTimeRequest(TARGET_START_ON, TARGET_END_ON),
                        null,
                        TARGET_CONTENTS
                ),
                new DailyLogUpdateRequest(
                        TARGET_LOG_ON,
                        new FocusTimeRequest(TARGET_START_ON, TARGET_END_ON, -1L, false),
                        STUDY,
                        TARGET_CONTENTS
                ),
                new DailyLogUpdateRequest(TARGET_LOG_ON, createFocusTimeRequest(), STUDY, " "),
                new DailyLogUpdateRequest(TARGET_LOG_ON, createFocusTimeRequest(), STUDY, null)
        );
    }
}
