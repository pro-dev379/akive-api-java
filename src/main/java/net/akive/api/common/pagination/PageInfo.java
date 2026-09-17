package net.akive.api.common.pagination;

public record PageInfo(
        int page,
        int size,
        long totalElement,
        int totalPage,
        boolean first,
        boolean last
) {

    public PageInfo {
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
    }
}
