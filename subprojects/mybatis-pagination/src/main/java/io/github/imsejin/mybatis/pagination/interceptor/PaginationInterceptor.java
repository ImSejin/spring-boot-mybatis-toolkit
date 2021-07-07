package io.github.imsejin.mybatis.pagination.interceptor;

import io.github.imsejin.mybatis.pagination.constant.RebuildMode;
import io.github.imsejin.mybatis.pagination.dialect.Dialect;
import io.github.imsejin.mybatis.pagination.model.PageInfo;
import io.github.imsejin.mybatis.pagination.model.Pageable;
import io.github.imsejin.mybatis.pagination.model.Paginator;
import io.github.imsejin.mybatis.pagination.support.InterceptorSupport;
import io.github.imsejin.mybatis.pagination.support.rebuilder.Rebuilder;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * MyBatis interceptor for auto pagination
 * <p>
 * If you define a mapper method that meets the conditions below,
 * this executes 2 queries(the total number of items and offset/limit).
 * These are that conditions.
 *
 * <ol>
 *     <li>Is return type {@link Paginator}?</li>
 *     <li>Is {@link Pageable} type or its implementation type in the parameters?</li>
 * </ol>
 * Only these defined methods will be affected by this interceptor.
 *
 * <pre>{@code
 *     Paginator<T> selectItems(Pageable pageable);
 * }</pre>
 * What you should be careful about when writing a query are
 *
 * <ol>
 *     <li>
 *         <h3>Don't use mapped parameter at comment</h3>
 *         If you use mapped parameter provided by MyBatis at
 *         single-line comment(--) or multi-line comment({@literal /}**{@literal /}),
 *         you'll fail to execute a query. While parsing SQL,
 *         all comments are removed and An exception occurs because
 *         unmapped parameters exist in {@link BoundSql#getParameterMappings()}.
 *     </li>
 *     <li>
 *         <h3>Don't need to use the keyword "OFFSET" or "LIMIT" at root query</h3>
 *         You don't have to insert them because this interceptor will
 *         automatically insert and process the pagination.
 *     </li>
 * </ol>
 *
 * <hr>
 * You only set these types to {@link Signature#type()}.
 * <ul>
 *     <li>{@link Executor}</li>
 *     <li>{@link ParameterHandler}</li>
 *     <li>{@link ResultSetHandler}</li>
 *     <li>{@link StatementHandler}</li>
 * </ul>
 * <p>
 * This interceptor handle this {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler)}.
 * That method is invoked on SELECT query.
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {
                MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class
        })
})
@RequiredArgsConstructor
public class PaginationInterceptor implements Interceptor {

    private static final int MAPPED_STATEMENT_INDEX = 0;
    private static final int PARAMETER_INDEX = 1;
    private static final int ROW_BOUNDS_INDEX = 2;
    private static final int RESULT_HANDLER_INDEX = 3;

    private final Dialect dialect;

    private Properties properties;

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Executor executor = (Executor) invocation.getTarget();
        MappedStatement ms = (MappedStatement) invocation.getArgs()[MAPPED_STATEMENT_INDEX];
        Object param = invocation.getArgs()[PARAMETER_INDEX];
        RowBounds rowBounds = (RowBounds) invocation.getArgs()[ROW_BOUNDS_INDEX];
        ResultHandler<?> resultHandler = (ResultHandler<?>) invocation.getArgs()[RESULT_HANDLER_INDEX];

        // Checks if the mapper method will be paginated.
        Method mapperMethod = InterceptorSupport.findMethod(ms);
        if (mapperMethod == null) return invocation.proceed();

        BoundSql boundSql = ms.getBoundSql(param);
        Configuration config = ms.getConfiguration();

        // Creates pagination query.
        Pageable pageable = InterceptorSupport.getPageableFromParam(param);
        BoundSql itemsBoundSql = this.dialect.createOffsetLimitBoundSql(boundSql, config, pageable);

        // Executes pagination query.
        MappedStatement itemsMs = wrap(ms, itemsBoundSql, ms.getResultMaps(), "items");
        List<?> items = executor.query(itemsMs, itemsBoundSql.getParameterObject(), rowBounds, resultHandler);

        // Creates total count query.
        BoundSql countBoundSql = this.dialect.createCountBoundSql(boundSql, config);

        // Executes total count query.
        ResultMap countResultMap = new ResultMap.Builder(config, "", Long.class, Collections.emptyList()).build();
        List<ResultMap> countResultMaps = Collections.singletonList(countResultMap);
        MappedStatement countMs = wrap(ms, countBoundSql, countResultMaps, "count");
        long totalItems = (Long) executor.query(countMs, countBoundSql.getParameterObject(), rowBounds, resultHandler)
                .get(0);

        return new Paginator<>(items, new PageInfo((int) totalItems, pageable));
    }

    /**
     * Returns new {@link MappedStatement} instance.
     *
     * @param ms         mapped statement
     * @param boundSql   bound SQL
     * @param resultMaps result maps
     * @param suffix     suffix for id
     * @return new {@link MappedStatement} instance
     */
    private static MappedStatement wrap(MappedStatement ms, BoundSql boundSql, List<ResultMap> resultMaps,
                                        String suffix) {
        SqlSource sqlSource = Rebuilder.init(ms.getConfiguration()).boundSql(boundSql).rebuild();

        return Rebuilder.init(ms, RebuildMode.WRAP).sqlSource(sqlSource)
                .resultMaps(resultMaps).suffix(suffix).rebuild();
    }

}
