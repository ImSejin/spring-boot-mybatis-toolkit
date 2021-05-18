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
