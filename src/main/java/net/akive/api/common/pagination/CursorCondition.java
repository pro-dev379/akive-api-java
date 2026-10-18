package net.akive.api.common.pagination;

public record CursorCondition<C>(C cursor, int size) {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public CursorCondition {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive.");
        }

        if (size > MAX_SIZE) {
            throw new IllegalArgumentException("Size must not exceed " + MAX_SIZE + ".");
        }
    }

    public static <C> CursorCondition<C> of(C cursor, Integer size) {
        return new CursorCondition<>(
                cursor,
                size != null ? size : DEFAULT_SIZE
        );
    }
}
