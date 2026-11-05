package net.akive.api.common.cache;

import lombok.NoArgsConstructor;

import java.time.Duration;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ResponseCacheTtl {

    public static final Duration COMMON_CODE_LIST = Duration.ofHours(24);
    public static final Duration LEGAL_DOCUMENT = Duration.ofDays(30);
    public static final Duration CURRENT_LEGAL_DOCUMENT = Duration.ofMinutes(30);
    public static final Duration LEGAL_DOCUMENT_HISTORY = Duration.ofHours(24);
    public static final Duration CURRENT_LEGAL_DOCUMENT_HEADERS = Duration.ofMinutes(30);
    public static final Duration ANALYTICS = Duration.ofMinutes(10);
}
