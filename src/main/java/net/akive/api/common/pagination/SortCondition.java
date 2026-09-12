package net.akive.api.common.pagination;

public record SortCondition(String property, SortDirection sortDirection) {

    public SortCondition {
        if (property == null || property.isBlank()) {
            throw new IllegalArgumentException("Sort property must not be blank.");
        }

        property = property.trim();

        if (sortDirection == null) {
            sortDirection = SortDirection.ASC;
        }
    }

    public static SortCondition of(String property) {
        return new SortCondition(property, SortDirection.ASC);
    }
}
