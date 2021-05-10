package io.github.imsejin.template.webapp.core.database.mybatis.interceptor;

import io.github.imsejin.template.webapp.core.database.mybatis.dialect.Dialect;
import io.github.imsejin.template.webapp.core.database.mybatis.model.pagination.Page;
import io.github.imsejin.template.webapp.core.database.mybatis.model.pagination.Pageable;
import io.github.imsejin.template.webapp.core.database.mybatis.model.pagination.Paginator;
import io.github.imsejin.template.webapp.core.database.mybatis.support.InterceptorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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
@RequiredArgsConstructor
public class PaginatorInterceptor implements Interceptor {

    private static final int MAPPED_STATEMENT_INDEX = 0;
    private static final int PARAMETER_INDEX = 1;
    private static final int ROW_BOUNDS_INDEX = 2;

    private final Dialect dialect;

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
        BoundSql countBoundSql = this.dialect.createCountBoundSql(boundSql, ms.getConfiguration());;
        log.info("countBoundSql: {}", countBoundSql.getSql());

        StaticSqlSource sqlSource = new StaticSqlSource(ms.getConfiguration(), countBoundSql.getSql(), countBoundSql.getParameterMappings());
        MappedStatement countMs = createCountMappedStatement(ms, sqlSource);

        // Executes total count query.
        invocation.getArgs()[0] = countMs;
        long totalItems = ((List<Long>) invocation.proceed()).get(0);

        // Executes items query.
        invocation.getArgs()[0] = ms;
        Object resultSet = invocation.proceed();

        List<?> items = (List<?>) resultSet;
        Pageable pageable = InterceptorUtils.getPageableFromParam(param);
        return new Paginator<>(items, new Page((int) totalItems, pageable));
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
