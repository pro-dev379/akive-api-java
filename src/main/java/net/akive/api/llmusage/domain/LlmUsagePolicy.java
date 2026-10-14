package net.akive.api.llmusage.domain;

public final class LlmUsagePolicy {

    public static final int MODEL_NAME_MAX_LENGTH = 100;
    public static final long USER_DAILY_TOKEN_MAX_COUNT = 30_000L;

    private LlmUsagePolicy() {
    }
}
