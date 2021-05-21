package io.github.imsejin.mybatis.pagination.dialect;

import io.github.imsejin.mybatis.pagination.constant.RebuildMode;
import io.github.imsejin.mybatis.pagination.model.Pageable;
import io.github.imsejin.mybatis.pagination.support.InterceptorSupport;
import io.github.imsejin.mybatis.pagination.support.rebuilder.Rebuilder;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.statement.select.*;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class MySQLDialect implements Dialect {

    @Override
    public BoundSql createCountBoundSql(BoundSql origin, Configuration config) {
        PlainSelect select = InterceptorSupport.parseSelect(origin.getSql());

        List<ParameterMapping> parameterMappings = getFilteredParameterMappings(origin, select);

        // Add "COUNT(*)" as select item into root select.
        Function countFunc = new Function();
        countFunc.setName("COUNT");
        countFunc.setAllColumns(true);
        List<SelectItem> selectItems = Collections.singletonList(new SelectExpressionItem(countFunc));
        select.setSelectItems(selectItems);

        // Removes statements "ORDER BY", "LIMIT", "OFFSET".
        select.setOrderByElements(null);
        select.setLimit(null);
        select.setOffset(null);

        return Rebuilder.init(origin, RebuildMode.WRAP).config(config)
                .sql(select.toString()).parameterMappings(parameterMappings).rebuild();
    }

    @Override
    public BoundSql createOffsetLimitBoundSql(BoundSql origin, Configuration config, Pageable pageable) {
        PlainSelect select = InterceptorSupport.parseSelect(origin.getSql());

        Limit limit = new Limit();
        limit.setRowCount(new LongValue(pageable.getLimit()));
        select.setLimit(limit);

        Offset offset = new Offset();
        offset.setOffset(pageable.getOffset());
        select.setOffset(offset);

        return Rebuilder.init(origin, RebuildMode.WRAP).config(config).sql(select.toString()).rebuild();
    }

    private static List<ParameterMapping> getFilteredParameterMappings(BoundSql boundSql, PlainSelect select) {
        List<ParameterMapping> mappings = new ArrayList<>(boundSql.getParameterMappings());

        // Indexes of parameter mappings in "SELECT" statement.
        List<Integer> indexes = IntStream.range(0, select.getSelectItems().stream().map(Object::toString)
                .mapToInt(it -> StringUtils.countOccurrencesOf(it, MAPPED_PARAMETER_CHARACTER)).sum())
                .boxed().collect(toList());

        int count = 0;

        Offset offset = select.getOffset();
        if (offset != null) {
            count += StringUtils.countOccurrencesOf(offset.toString(), MAPPED_PARAMETER_CHARACTER);
        }

        Limit limit = select.getLimit();
        if (limit != null) {
            count += StringUtils.countOccurrencesOf(limit.toString(), MAPPED_PARAMETER_CHARACTER);
        }

        List<OrderByElement> orderBy = select.getOrderByElements();
        if (!CollectionUtils.isEmpty(orderBy)) {
            for (OrderByElement order : orderBy) {
                count += StringUtils.countOccurrencesOf(order.toString(), MAPPED_PARAMETER_CHARACTER);
            }
        }

        // Adds indexes of parameter mappings in "OFFSET", "LIMIT", "ORDER BY" statements.
        int lastIndex = mappings.size() - 1;
        indexes.addAll(IntStream.range(lastIndex - count, lastIndex).boxed().collect(toList()));

        // Removes parameter mappings in that statements.
        indexes.forEach(i -> mappings.set(i, null));
        return mappings.stream().filter(Objects::nonNull).collect(toList());
    }

}
