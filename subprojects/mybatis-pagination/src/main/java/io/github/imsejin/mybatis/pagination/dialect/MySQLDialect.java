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
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class MySQLDialect implements Dialect {

    @Override
    public BoundSql createCountBoundSql(BoundSql origin, Configuration config) {
        PlainSelect select = InterceptorSupport.parseSelect(origin.getSql());

        List<ParameterMapping> parameterMappings = getFilteredParameterMappings(origin, select);

        // Add 'COUNT(*)' as select item into root select.
        Function countFunc = new Function();
        countFunc.setName("COUNT");
        countFunc.setAllColumns(true);
        List<SelectItem> selectItems = Collections.singletonList(new SelectExpressionItem(countFunc));
        select.setSelectItems(selectItems);

        // Removes statements 'ORDER BY', 'LIMIT', 'OFFSET'.
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
        ParameterMapping[] mappings = boundSql.getParameterMappings().toArray(new ParameterMapping[0]);

        // Removes parameter mappings in select statement.
        int selelctOccurrences = 0;
        for (SelectItem selectItem : select.getSelectItems()) {
            selelctOccurrences += StringUtils.countOccurrencesOf(selectItem.toString(), "?");
        }
        for (int i = 0; i < selelctOccurrences; i++) {
            mappings[i] = null;
        }

        int otherOccurrences = 0;

        // Removes parameter mappings in offset statement.
        if (select.getOffset() != null) {
            int lastIndex = (mappings.length - 1) - otherOccurrences;
            int occurrences = StringUtils.countOccurrencesOf(select.getLimit().toString(), "?");
            otherOccurrences += occurrences;

            for (int i = lastIndex; i > lastIndex - occurrences && i >= selelctOccurrences; i--) {
                mappings[i] = null;
            }
        }

        // Removes parameter mappings in limit statement.
        if (select.getLimit() != null) {
            int lastIndex = (mappings.length - 1) - otherOccurrences;
            int occurrences = StringUtils.countOccurrencesOf(select.getLimit().toString(), "?");
            otherOccurrences += occurrences;

            for (int i = lastIndex; i > lastIndex - occurrences && i >= selelctOccurrences; i--) {
                mappings[i] = null;
            }
        }

        // Removes parameter mappings in order-by statement.
        if (select.getOrderByElements() != null && !select.getOrderByElements().isEmpty()) {
            int lastIndex = (mappings.length - 1) - otherOccurrences;
            int occurrences = 0;
            for (OrderByElement orderByElement : select.getOrderByElements()) {
                occurrences += StringUtils.countOccurrencesOf(orderByElement.toString(), "?");
            }

            for (int i = lastIndex; i > lastIndex - occurrences && i >= selelctOccurrences; i--) {
                mappings[i] = null;
            }
        }

        return Arrays.stream(mappings).filter(Objects::nonNull).collect(toList());
    }

}
