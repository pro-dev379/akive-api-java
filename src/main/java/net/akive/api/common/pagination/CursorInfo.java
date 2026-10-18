package net.akive.api.common.pagination;

public record CursorInfo<C>(C nextCursor, boolean hasNext) {

    public CursorInfo {
        if (hasNext && nextCursor == null) {
            throw new IllegalArgumentException("Next cursor must not be null when next content exists.");
        }
    }
}
