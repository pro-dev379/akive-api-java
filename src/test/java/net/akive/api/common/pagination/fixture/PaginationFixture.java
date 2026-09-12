package net.akive.api.common.pagination.fixture;

import net.akive.api.common.pagination.PageResult;

import java.util.List;

public class PaginationFixture {

    public static final int PAGE = 1;
    public static final int SIZE = 20;
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 7;
    public static final int MAX_SIZE = 100;
    public static final List<String> CONTENTS = List.of("a", "b");
    public static final long TOTAL_ELEMENT = CONTENTS.size();
    public static final int TOTAL_PAGE = calculateTotalPage(DEFAULT_SIZE, CONTENTS);
    public static final String PROPERTY = "createdAt";

    public static PageResult<String> createPageResult() {
        return createPageResult(DEFAULT_PAGE, CONTENTS.size(), CONTENTS);
    }

    public static <T> PageResult<T> createPageResult(List<T> contents) {
        return createPageResult(DEFAULT_PAGE, contents.size(), contents);
    }

    public static <T> PageResult<T> createPageResult(int page, int size, List<T> contents) {
        int totalPage = calculateTotalPage(size, contents);

        return new PageResult<>(
                page,
                size,
                contents.size(),
                totalPage,
                page == 0,
                page >= totalPage - 1,
                contents
        );
    }

    private static int calculateTotalPage(int size, List<?> contents) {
        return (int) Math.ceil((double) contents.size() / size);
    }
}
