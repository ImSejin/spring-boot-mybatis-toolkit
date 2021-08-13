package io.github.imsejin.mybatis.pagination.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.imsejin.mybatis.pagination.serializer.PaginatorSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Collections;
import java.util.List;

/**
 * Paginator for paginated query result.
 * <p>
 * Return type of {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler)}
 * is {@link List}, so this must implement that type.
 *
 * @param <T> type
 */
@Getter
@ToString
@EqualsAndHashCode
@JsonSerialize(using = PaginatorSerializer.class)
public class Paginator<T> implements List<T> {

    @Delegate
    private final List<T> items;

    private final PageInfo pageInfo;

    public Paginator(List<T> items, PageInfo pageInfo) {
        this.items = items == null ? Collections.emptyList() : Collections.unmodifiableList(items);
        this.pageInfo = pageInfo;
    }

}
