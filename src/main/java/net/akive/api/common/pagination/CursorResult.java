package net.akive.api.common.pagination;

import java.util.List;

public record CursorResult<T, C>(CursorInfo<C> cursorInfo, List<T> contents) {

    public CursorResult {
        if (cursorInfo == null) {
            throw new IllegalArgumentException("Cursor info must not be null.");
        }

        if (contents == null) {
            throw new IllegalArgumentException("Contents must not be null.");
        }

        contents = List.copyOf(contents);
    }

    public static <T, C> CursorResult<T, C> of(
            C nextCursor,
            boolean hasNext,
            List<T> contents
    ) {
        return new CursorResult<>(
                new CursorInfo<>(nextCursor, hasNext),
                contents
        );
    }
}
