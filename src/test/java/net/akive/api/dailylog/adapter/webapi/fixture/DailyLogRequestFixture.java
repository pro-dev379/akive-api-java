package net.akive.api.dailylog.adapter.webapi.fixture;

import net.akive.api.dailylog.adapter.webapi.dto.request.DailyLogCreateRequest;
import net.akive.api.dailylog.adapter.webapi.dto.request.DailyLogUpdateRequest;
import net.akive.api.dailylog.adapter.webapi.dto.request.ActivityTimeRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static net.akive.api.dailylog.domain.DailyLogType.STUDY;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;

public class DailyLogRequestFixture {

    private DailyLogRequestFixture() {
    }

    public static DailyLogCreateRequest createDailyLogCreateRequest() {
        return createDailyLogCreateRequest(createActivityTimeRequest(), FOCUS_TIME_SEC);
    }

    public static DailyLogCreateRequest createDailyLogCreateRequest(ActivityTimeRequest activityTimeRequest) {
        return createDailyLogCreateRequest(
                activityTimeRequest,
                Duration.between(activityTimeRequest.startOn(), activityTimeRequest.endOn()).toSeconds()
        );
    }

    public static DailyLogCreateRequest createDailyLogCreateRequest(
            ActivityTimeRequest activityTimeRequest,
            Long focusTimeSec
    ) {
        return new DailyLogCreateRequest(
                new ActivityTimeRequest(
                        activityTimeRequest.startOn(),
                        activityTimeRequest.endOn(),
                        focusTimeSec,
                        activityTimeRequest.focusRateExcluded()
                ),
                STUDY,
                CONTENTS
        );
    }

    public static ActivityTimeRequest createActivityTimeRequest() {
        return createActivityTimeRequest(START_ON, END_ON);
    }

    public static DailyLogUpdateRequest createDailyLogUpdateRequest() {
        return createDailyLogUpdateRequest(createActivityTimeRequest(TARGET_START_ON, TARGET_END_ON));
    }

    public static DailyLogUpdateRequest createDailyLogUpdateRequest(ActivityTimeRequest targetActivityTime) {
        return new DailyLogUpdateRequest(targetActivityTime, STUDY, TARGET_CONTENTS);
    }

    public static DailyLogUpdateRequest createDailyLogUpdateRequest(
            ActivityTimeRequest targetActivityTime,
            Long focusTimeSec
    ) {
        return createDailyLogUpdateRequest(
                new ActivityTimeRequest(
                        targetActivityTime.startOn(),
                        targetActivityTime.endOn(),
                        focusTimeSec,
                        targetActivityTime.focusRateExcluded()
                )
        );
    }

    public static ActivityTimeRequest createActivityTimeRequest(LocalDateTime startOn, LocalDateTime endOn) {
        return new ActivityTimeRequest(startOn, endOn, FOCUS_TIME_SEC, false);
    }

    public static Stream<DailyLogCreateRequest> invalidCreateRequests() {
        return Stream.of(
                new DailyLogCreateRequest(
                        null,
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        new ActivityTimeRequest(null, END_ON, FOCUS_TIME_SEC, false),
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        new ActivityTimeRequest(START_ON, null, FOCUS_TIME_SEC, false),
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        createActivityTimeRequest(),
                        null,
                        CONTENTS
                ),
                new DailyLogCreateRequest(
                        new ActivityTimeRequest(START_ON, END_ON, -1L, false),
                        STUDY,
                        CONTENTS
                ),
                new DailyLogCreateRequest(createActivityTimeRequest(), STUDY, " "),
                new DailyLogCreateRequest(createActivityTimeRequest(), STUDY, null)
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
                        new ActivityTimeRequest(null, TARGET_END_ON, FOCUS_TIME_SEC, false),
                        STUDY,
                        TARGET_CONTENTS
                ),
                new DailyLogUpdateRequest(
                        new ActivityTimeRequest(TARGET_START_ON, null, FOCUS_TIME_SEC, false),
                        STUDY,
                        TARGET_CONTENTS
                ),
                new DailyLogUpdateRequest(
                        createActivityTimeRequest(TARGET_START_ON, TARGET_END_ON),
                        null,
                        TARGET_CONTENTS
                ),
                new DailyLogUpdateRequest(
                        new ActivityTimeRequest(TARGET_START_ON, TARGET_END_ON, -1L, false),
                        STUDY,
                        TARGET_CONTENTS
                ),
                new DailyLogUpdateRequest(createActivityTimeRequest(), STUDY, " "),
                new DailyLogUpdateRequest(createActivityTimeRequest(), STUDY, null)
        );
    }
}
