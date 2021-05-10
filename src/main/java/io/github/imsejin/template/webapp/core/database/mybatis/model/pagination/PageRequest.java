package io.github.imsejin.template.webapp.core.database.mybatis.model.pagination;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Setter
@ToString
public class PageRequest implements Pageable {

    @JsonProperty("page")
    private int offset;

    @JsonProperty("size")
    private int limit;

    @JsonProperty("query")
    private Map<String, Object> query;

    @Override
    public int getOffset() {
        return Math.max(0, this.offset - 1) * this.limit;
    }

    @Override
    public int getLimit() {
        return this.limit;
    }

    public Map<String, Object> getQuery() {
        return query == null ? new HashMap<>() : query;
    }

}
