package net.akive.api.user.domain;

import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.regex.Pattern;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class UserPolicy {

    public static final int NICKNAME_MAX_LENGTH = 20;
    public static final int ROLE_BASED_NICKNAME_SUFFIX_LENGTH = 10;

    public static final int AIM_MAX_LENGTH = 100;

    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final String PASSWORD_PATTERN_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).*$";
    public static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_PATTERN_REGEX);

    public static final int LEAVE_REASON_MAX_LENGTH = 300;

    public static final LocalTime DEFAULT_GOAL_ATTEND_ON = LocalTime.of(20, 0);
    public static final long DEFAULT_GOAL_REST_10_MIN_SEC = 120L;
    public static final int DEFAULT_GOAL_MEAL_NUM = 3;
    public static final long DEFAULT_GOAL_MEAL_1_TIME_SEC = 3_600L;
}
