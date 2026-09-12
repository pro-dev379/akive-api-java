package net.akive.api.common.pagination;

import java.util.List;

public record PageResult<T>(
        int page,
        int size,
        long totalElement,
        int totalPage,
        boolean first,
        boolean last,
        List<T> contents
) {

    public PageResult {
        if (page < 0) {
            throw new IllegalArgumentException("Page must not be negative.");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive.");
        }

        if (totalElement < 0) {
            throw new IllegalArgumentException("Total element must not be negative.");
        }

        if (totalPage < 0) {
            throw new IllegalArgumentException("Total page must not be negative.");
        }

        if (contents == null) {
            throw new IllegalArgumentException("Contents must not be null.");
        }

        if (contents.size() > size) {
            throw new IllegalArgumentException("Contents size must not exceed page size.");
        }

        contents = List.copyOf(contents);
    }
}
