package io.github.imsejin.mybatis.pagination.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * Request model of pagination.
 */
@ToString
public class PageRequest implements Pageable {

    public static final String QUERY_PROPERTY_NAME = "query";

    @JsonProperty("page")
    private int offset;

    @JsonProperty("size")
    private int limit;

    @Setter
    @JsonProperty(QUERY_PROPERTY_NAME)
    private Map<String, Object> query;

    /**
     * @see PageInfo#getPage()
     * @see PageInfo#getOffset()
     */
    @Override
    public int getOffset() {
        return Math.max(0, this.offset - 1) * this.limit;
    }

    public void setOffset(int offset) {
        if (offset < 0) throw new IllegalArgumentException("PageRequest.offset should not be negative: " + offset);
        this.offset = offset;
    }

    /**
     * @see PageInfo#getSize()
     * @see PageInfo#getLimit()
     */
    @Override
    public int getLimit() {
        return this.limit;
    }

    public void setLimit(int limit) {
        if (limit < 1) throw new IllegalArgumentException("PageRequest.limit must be positive: " + limit);
        this.limit = limit;
    }

    public Map<String, Object> getQuery() {
        return query == null ? new HashMap<>() : query;
    }

}
