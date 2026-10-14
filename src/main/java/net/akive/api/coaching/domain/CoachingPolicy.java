package net.akive.api.coaching.domain;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class CoachingPolicy {

    public static final int USER_MESSAGE_CONTENT_MAX_LENGTH = 1000;

    public static final long COACHING_OUTPUT_TOKEN_MAX_COUNT = 2048L;
    public static final long TITLE_OUTPUT_TOKEN_MAX_COUNT = 512L;
}
