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
 * {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler)}의
 * 리턴 값이 {@link List}이기에 'polymorphism'이 적용될 수 있게 {@link List}를
 * 구현해야 한다.
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
        this.items = items == null ? Collections.emptyList() : items;
        this.pageInfo = pageInfo;
    }

}
