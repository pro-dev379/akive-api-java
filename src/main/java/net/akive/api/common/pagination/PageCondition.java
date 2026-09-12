package net.akive.api.common.pagination;

import lombok.Getter;

@Getter
public final class PageCondition {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 7;
    private static final int MAX_SIZE = 100;

    private final int page;
    private final int size;
    private final SortCondition sortCondition;

    private PageCondition(Integer page, Integer size, SortCondition sortCondition) {
        this.page = page != null ? page : DEFAULT_PAGE;
        this.size = size != null ? size : DEFAULT_SIZE;
        this.sortCondition = sortCondition;

        validate();
    }

    public static PageCondition of(String property) {
        return of(null, null, property);
    }

    public static PageCondition of(Integer page, Integer size, String property) {
        return new PageCondition(page, size, SortCondition.of(property));
    }

    public static PageCondition of(Integer page, Integer size, String property, SortDirection sortDirection) {
        return new PageCondition(page, size, new SortCondition(property, sortDirection));
    }

    private void validate() {
        if (this.page < 0) {
            throw new IllegalArgumentException("Page must not be negative.");
        }

        if (this.size <= 0) {
            throw new IllegalArgumentException("Size must be positive.");
        }

        if (this.size > MAX_SIZE) {
            throw new IllegalArgumentException("Size must not exceed " + MAX_SIZE + ".");
        }

        if (this.sortCondition == null) {
            throw new IllegalArgumentException("Sort condition must not be null.");
        }
    }
}
