package io.github.imsejin.template.webapp.core.database.mybatis.interceptor;

import io.github.imsejin.template.webapp.core.database.mybatis.model.pagination.Page;
import io.github.imsejin.template.webapp.core.database.mybatis.model.pagination.Paginator;
import io.github.imsejin.template.webapp.core.database.mybatis.support.InterceptorUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * <p> type
 * <ul>
 *     <li>{@link Executor}</li>
 *     <li>{@link ParameterHandler}</li>
 *     <li>{@link ResultSetHandler}</li>
 *     <li>{@link StatementHandler}</li>
 * </ul>
 *
 * <p> {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler)}
 *
 * <p> Values of {@link Signature#method()}
 * <dl>
 *     <dt>SELECT</dt>
 *     <dd>query</dd>
 *     <dt>INSERT</dt>
 *     <dd>update</dd>
 *     <dt>UPDATE</dt>
 *     <dd>update</dd>
 *     <dt>DELETE</dt>
 *     <dd>update</dd>
 * </dl>
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {
                MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class
        })
})
@Slf4j
public class PaginatorInterceptor implements Interceptor {

    private static final int MAPPED_STATEMENT_INDEX = 0;
    private static final int PARAMETER_INDEX = 1;
    private static final int ROW_BOUNDS_INDEX = 2;

    private Properties properties;

    @Override
    public void setProperties(Properties properties) {
        log.info("===== properties: {}", properties);
        this.properties = properties;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object intercept(Invocation invocation) throws Throwable {
        log.info("argument types: {}", Arrays.toString(Arrays.stream(invocation.getArgs())
                .map(it -> it == null ? "null" : it.getClass().getSimpleName()).toArray()));
        log.info("arguments: {}", Arrays.toString(invocation.getArgs()));

        MappedStatement ms = (MappedStatement) invocation.getArgs()[MAPPED_STATEMENT_INDEX];
        Object param = invocation.getArgs()[PARAMETER_INDEX];
//        RowBounds rowBounds = (RowBounds) invocation.getArgs()[ROW_BOUNDS_INDEX];

        Method mapperMethod = InterceptorUtils.findMethod(ms);
        if (mapperMethod == null) return invocation.proceed();

        BoundSql boundSql = ms.getBoundSql(param);
        log.info("=================== boundSql: {}", boundSql.getSql());

        // Manipulates query.
        BoundSql countBoundSql = createCountBoundSql(boundSql, ms.getConfiguration());
        log.info("countBoundSql: {}", countBoundSql.getSql());

        StaticSqlSource sqlSource = new StaticSqlSource(ms.getConfiguration(), countBoundSql.getSql(), countBoundSql.getParameterMappings());
        MappedStatement countMs = createCountMappedStatement(ms, sqlSource);
        countBoundSql.getParameterMappings().forEach(System.out::println);

        // Executes total count query.
        invocation.getArgs()[0] = countMs;
        long totalItems = ((List<Long>) invocation.proceed()).get(0);
        log.info("=== totalItems: {}", totalItems);

        invocation.getArgs()[0] = ms;
        Object resultSet = invocation.proceed();
        log.info("=== resultSet: {}", resultSet);

        List<?> items = (List<?>) resultSet;
        Page page = InterceptorUtils.getPageFromParam(param);
        return new Paginator<>(items, new Page((int) totalItems, page.getNumber(), page.getSize()));
    }

    private static BoundSql createCountBoundSql(BoundSql boundSql, Configuration config) {
        PlainSelect select = InterceptorUtils.newSelect(boundSql.getSql());
        List<ParameterMapping> mappings = getFilteredParameterMappings(boundSql, select);
        newCountSelect(boundSql, select);
        return new BoundSql(config, select.toString(), mappings, boundSql.getParameterObject());
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

    private static PlainSelect newCountSelect(BoundSql boundSql, PlainSelect select) {
//        List<ParameterMapping> mappings = getFilteredParameterMappings(boundSql, select);

        // log.info("mappings: {}", mappings);
        // log.info("=================== boundSql.getParameterMappings({}): {}", boundSql.getParameterMappings().size(),
        //         boundSql.getParameterMappings().stream().map(ParameterMapping::getProperty).collect(toList()));

//        boundSql.getParameterMappings().clear();
//        boundSql.getParameterMappings().addAll(mappings);

        // log.info("=================== boundSql.getParameterMappings({}): {}", boundSql.getParameterMappings().size(),
        //         boundSql.getParameterMappings().stream().map(ParameterMapping::getProperty).collect(toList()));

        /*
        Converts to count query.
         */
        log.info("========== before select: {}", select);

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

        log.info("========== after select: {}", select);

        return select;
    }

    private static List<ResultMap> createCountResultMaps(MappedStatement ms) {
        List<ResultMap> countResultMaps = new ArrayList<>();

        ResultMap countResultMap =
                new ResultMap.Builder(ms.getConfiguration(), ms.getId() + "$count", Long.class, new ArrayList<>())
                        .build();
        countResultMaps.add(countResultMap);

        return countResultMaps;
    }

    private MappedStatement createCountMappedStatement(MappedStatement ms, SqlSource source) {
        List<ResultMap> countResultMaps = createCountResultMaps(ms);

        return new MappedStatement.Builder(ms.getConfiguration(), ms.getId() + "$count",
                source, ms.getSqlCommandType())
                .resource(ms.getResource())
                .parameterMap(ms.getParameterMap())
                .resultMaps(countResultMaps)
                .fetchSize(ms.getFetchSize())
                .timeout(ms.getTimeout())
                .statementType(ms.getStatementType())
                .resultSetType(ms.getResultSetType())
                .cache(ms.getCache())
                .flushCacheRequired(ms.isFlushCacheRequired())
                .useCache(true)
                .resultOrdered(ms.isResultOrdered())
                .keyGenerator(ms.getKeyGenerator())
                .keyColumn(ms.getKeyColumns() != null ? String.join(",", ms.getKeyColumns()) : null)
                .keyProperty(ms.getKeyProperties() != null ? String.join(",", ms.getKeyProperties()) : null)
                .databaseId(ms.getDatabaseId())
                .lang(ms.getLang())
                .resultSets(ms.getResultSets() != null ? String.join(",", ms.getResultSets()) : null)
                .build();
    }

}
