package net.akive.api.common.pagination;

import java.util.List;

public record PageResult<T>(
        PageInfo pageInfo,
        List<T> contents
) {

    public PageResult {
        if (pageInfo == null) {
            throw new IllegalArgumentException("Page info must not be null.");
        }

        if (contents == null) {
            throw new IllegalArgumentException("Contents must not be null.");
        }

        if (contents.size() > pageInfo.size()) {
            throw new IllegalArgumentException("Contents size must not exceed page size.");
        }

        contents = List.copyOf(contents);
    }

    public static <T> PageResult<T> of(
            int page,
            int size,
            long totalElement,
            int totalPage,
            boolean first,
            boolean last,
            List<T> contents
    ) {
        return new PageResult<>(
                new PageInfo(page, size, totalElement, totalPage, first, last),
                contents
        );
    }
}
