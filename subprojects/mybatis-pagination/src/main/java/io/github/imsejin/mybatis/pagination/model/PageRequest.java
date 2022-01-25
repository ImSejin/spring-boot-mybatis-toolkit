package io.github.imsejin.mybatis.pagination.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.imsejin.mybatis.pagination.resolver.PageRequestResolver;
import lombok.Getter;
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

    @Getter
    @JsonProperty("page")
    private int page;

    @Getter
    @JsonProperty("size")
    private int size;

    @Setter
    @JsonProperty(QUERY_PROPERTY_NAME)
    private Map<String, Object> query;

    @Override
    public int getOffset() {
        return Math.max(0, this.page - 1) * this.size;
    }

    @Override
    public int getLimit() {
        return this.size;
    }

    @Override
    public int getStartRowNum() {
        return ((this.page - 1) * this.size) + 1;
    }

    @Override
    public int getEndRowNum() {
        return this.page * this.size;
    }

    /**
     * @see PageRequestResolver
     */
    public void setPage(int page) {
        if (page < 0) throw new IllegalArgumentException("PageRequest.page should not be negative: " + page);
        this.page = page;
    }

    /**
     * @see PageRequestResolver
     */
    public void setSize(int size) {
        if (size < 1) throw new IllegalArgumentException("PageRequest.size must be positive: " + size);
        this.size = size;
    }

    public Map<String, Object> getQuery() {
        return query == null ? new HashMap<>() : query;
    }

}
