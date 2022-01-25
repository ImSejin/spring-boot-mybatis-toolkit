package io.github.imsejin.mybatis.pagination.dialect;

import io.github.imsejin.mybatis.pagination.constant.RebuildMode;
import io.github.imsejin.mybatis.pagination.model.Pageable;
import io.github.imsejin.mybatis.pagination.support.InterceptorSupport;
import io.github.imsejin.mybatis.pagination.support.rebuilder.Rebuilder;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
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

public class OracleDialect implements Dialect {

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

        // Removes statements "ORDER BY", "OFFSET", "FETCH", "FIRST".
        select.setOrderByElements(null);
        select.setOffset(null);
        select.setFetch(null);
        select.setFirst(null);

        return Rebuilder.init(origin, RebuildMode.WRAP).config(config)
                .sql(select.toString()).parameterMappings(parameterMappings).rebuild();
    }

    @Override
    public BoundSql createOffsetLimitBoundSql(BoundSql origin, Configuration config, Pageable pageable) {
        PlainSelect select = InterceptorSupport.parseSelect(origin.getSql());

        // Creates BoundSql differently for improving performance.
        String sql;
        if (CollectionUtils.isEmpty(select.getOrderByElements())) {
            // When original query doesn't have ORDER BY statement.
            addRowLimitingClause(select, pageable);
            sql = select.toString();

        } else {
            // When original query has ORDER BY statement.
            SelectBody wrappedSelectBody = wrapSelectBodyWithMaxRowNum(select, pageable);
            SelectBody selectBody = wrapSelectBodyWithMinRowNum(wrappedSelectBody, pageable);
            sql = selectBody.toString();
        }

        return Rebuilder.init(origin, RebuildMode.WRAP).config(config).sql(sql).rebuild();
    }

    private static List<ParameterMapping> getFilteredParameterMappings(BoundSql boundSql, PlainSelect select) {
        List<ParameterMapping> mappings = new ArrayList<>(boundSql.getParameterMappings());

        // Indexes of parameter mappings in "SELECT" statement.
        List<Integer> indexes = IntStream.range(0, select.getSelectItems().stream().map(Object::toString)
                        .mapToInt(it -> StringUtils.countOccurrencesOf(it, MAPPED_PARAMETER_CHARACTER)).sum())
                .boxed().collect(toList());

        int count = 0;

        // OFFSET 0 ROWS FETCH FIRST 10 ROWS ONLY
        Offset offset = select.getOffset();
        if (offset != null) {
            count += StringUtils.countOccurrencesOf(offset.toString(), MAPPED_PARAMETER_CHARACTER);
        }

        Fetch fetch = select.getFetch();
        if (fetch != null) {
            count += StringUtils.countOccurrencesOf(fetch.toString(), MAPPED_PARAMETER_CHARACTER);
        }

        First first = select.getFirst();
        if (first != null) {
            count += StringUtils.countOccurrencesOf(first.toString(), MAPPED_PARAMETER_CHARACTER);
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

    private static void addRowLimitingClause(PlainSelect select, Pageable pageable) {
        Offset offset = new Offset();
        offset.setOffset(pageable.getOffset());
        offset.setOffsetParam("ROWS");
        select.setOffset(offset);

        Fetch fetch = new Fetch();
        fetch.setFetchParamFirst(true);
        fetch.setRowCount(pageable.getLimit());
        fetch.setFetchParam("ROWS");
        select.setFetch(fetch);
    }

    private static SelectBody wrapSelectBodyWithMinRowNum(SelectBody wrappedSelectBody, Pageable pageable) {
        PlainSelect body = new PlainSelect();

        // Makes FROM statement.
        SubSelect subSelect = new SubSelect();
        subSelect.setSelectBody(wrappedSelectBody);
        subSelect.setAlias(new Alias("_$WRAPPER", true));
        body.setFromItem(subSelect);

        // Makes SELECT statement.
        AllTableColumns allColumns = new AllTableColumns(new Table("_$WRAPPER"));
        body.addSelectItems(allColumns);

        // Makes WHERE statement.
        GreaterThanEquals gte = new GreaterThanEquals();
        Column rowNum = new Column("_$ROWNUM").withTable(new Table("_$WRAPPER"));
        gte.setLeftExpression(rowNum);
        gte.setRightExpression(new LongValue(pageable.getStartRowNum()));
        body.setWhere(gte);

        return body;
    }

    private static SelectBody wrapSelectBodyWithMaxRowNum(SelectBody originSelectBody, Pageable pageable) {
        PlainSelect body = new PlainSelect();

        // Makes FROM statement.
        SubSelect subSelect = new SubSelect();
        subSelect.setSelectBody(originSelectBody);
        subSelect.setAlias(new Alias("_$ORIGIN", true));
        body.setFromItem(subSelect);

        // Makes SELECT statement.
        SelectExpressionItem rowNum = new SelectExpressionItem(new Column("ROWNUM"));
        rowNum.setAlias(new Alias("_$ROWNUM", true));
        AllTableColumns originColumns = new AllTableColumns(new Table("_$ORIGIN"));
        body.addSelectItems(rowNum, originColumns);

        // Makes WHERE statement.
        MinorThanEquals mte = new MinorThanEquals();
        mte.setLeftExpression(new Column("ROWNUM"));
        mte.setRightExpression(new LongValue(pageable.getEndRowNum()));
        body.setWhere(mte);

        return body;
    }

}
