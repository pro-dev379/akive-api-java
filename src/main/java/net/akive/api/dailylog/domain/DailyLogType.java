package net.akive.api.dailylog.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DailyLogType {

    STUDY("공부"),
    SLEEP("수면"),
    REST("휴식"),
    MEAL("식사"),
    ATTENDANCE("출석"),
    ETC("기타"),
    ;

    private final String label;
}
