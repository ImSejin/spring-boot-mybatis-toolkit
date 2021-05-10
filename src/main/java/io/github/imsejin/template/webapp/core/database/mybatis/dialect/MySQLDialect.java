package io.github.imsejin.template.webapp.core.database.mybatis.dialect;

import io.github.imsejin.template.webapp.core.database.mybatis.support.InterceptorUtils;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class MySQLDialect implements Dialect {

    @Override
    public BoundSql createCountBoundSql(BoundSql originalBoundSql, Configuration config) {
        PlainSelect select = InterceptorUtils.newSelect(originalBoundSql.getSql());

        List<ParameterMapping> mappings = getFilteredParameterMappings(originalBoundSql, select);

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

        return new BoundSql(config, select.toString(), mappings, originalBoundSql.getParameterObject());
    }

    @Override
    public BoundSql createOffsetLimitBoundSql(BoundSql originalBoundSql, Configuration config) {
        return null;
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
        if (!CollectionUtils.isEmpty(select.getOrderByElements())) {
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
