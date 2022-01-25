package io.github.imsejin.mybatis.pagination.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Information of pagination.
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class PageInfo implements Pageable {

    /**
     * Minimum value is 0.
     */
    private final int totalItems;

    /**
     * Current page number.
     * Minimum value is 1.
     */
    private final int page;

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

    @JsonIgnore
    private final int startRowNum;

    @JsonIgnore
    private final int endRowNum;

    public PageInfo(int totalItems, int page, int size) {
        if (page < 1) throw new IllegalArgumentException("PageInfo.page must be positive: " + page);
        if (size < 1) throw new IllegalArgumentException("PageInfo.size must be positive: " + size);
        if (totalItems < 0) {
            throw new IllegalArgumentException("PageInfo.totalItems should not be negative: " + totalItems);
        }

        this.totalItems = totalItems;
        this.totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) size));

        this.page = Math.min(this.totalPages, page);
        this.size = size;

        this.offset = Math.max(0, page - 1) * size;
        this.limit = size;

        this.startRowNum = ((page - 1) * size) + 1;
        this.endRowNum = page * size;
    }

    public PageInfo(int totalItems, Pageable pageable) {
        this(totalItems, Math.max(0, pageable.getOffset() / pageable.getLimit()) + 1, pageable.getLimit());
    }

}
