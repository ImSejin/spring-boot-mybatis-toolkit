package io.github.imsejin.mybatis.pagination.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class Page implements Pageable {

    /**
     * Minimum value is 0.
     */
    private final int totalItems;

    /**
     * Current page number.
     * Minimum value is 1.
     */
    private final int number;

    /**
     * Number of contents per page.
     * Minimum value is 1.
     */
    private final int size;

    /**
     * Number of total pages.
     * Minimum value is 1.
     */
    private final int totalPages;

    @JsonIgnore
    private final int offset;

    @JsonIgnore
    private final int limit;

    public Page(int totalItems, int number, int size) {
        if (number < 1) throw new IllegalArgumentException("Page.number must be positive: " + number);
        if (size < 1) throw new IllegalArgumentException("Page.size must be positive: " + size);
        if (totalItems < 0) {
            throw new IllegalArgumentException("Page.totalItems should not be negative: " + totalItems);
        }

        this.totalItems = totalItems;
        this.totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) size));

        this.number = Math.min(this.totalPages, number);
        this.size = size;

        this.offset = Math.max(0, number - 1) * size;
        this.limit = size;
    }

    public Page(int totalItems, Pageable pageable) {
        this(totalItems, Math.max(0, pageable.getOffset() / pageable.getLimit()) + 1, pageable.getLimit());
    }

    @Override
    public String toString() {
        return "Page(" +
                "totalItems=" + totalItems +
                ", number=" + number +
                ", size=" + size +
                ", totalPages=" + totalPages +
                ", offset=" + getOffset() +
                ", limit=" + size +
                ')';
    }

}