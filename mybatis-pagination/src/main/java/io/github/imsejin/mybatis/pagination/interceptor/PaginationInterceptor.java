package io.github.imsejin.mybatis.pagination.interceptor;

import io.github.imsejin.mybatis.pagination.dialect.Dialect;
import io.github.imsejin.mybatis.pagination.model.PageInfo;
import io.github.imsejin.mybatis.pagination.model.Pageable;
import io.github.imsejin.mybatis.pagination.model.Paginator;
import io.github.imsejin.mybatis.pagination.support.InterceptorSupport;
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
    @SuppressWarnings("unchecked")
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[MAPPED_STATEMENT_INDEX];
        Object param = invocation.getArgs()[PARAMETER_INDEX];

        // Checks if the mapper method will be paginated.
        Method mapperMethod = InterceptorSupport.findMethod(ms);
        if (mapperMethod == null) return invocation.proceed();

        BoundSql boundSql = ms.getBoundSql(param);
        Configuration config = ms.getConfiguration();

        // Creates pagination query.
        Pageable pageable = InterceptorSupport.getPageableFromParam(param);
        BoundSql pagingBoundSql = this.dialect.createOffsetLimitBoundSql(boundSql, config, pageable);

        // Executes pagination query.
        invocation.getArgs()[0] = newMappedStatement(ms, pagingBoundSql, ms.getResultMaps(), ms.getId() + "$pagination",
                InterceptorSupport.findResultMapType(ms));
        Object resultSet = invocation.proceed();

        // Creates total count query.
        BoundSql countBoundSql = this.dialect.createCountBoundSql(boundSql, config);

        // Executes total count query.
        ResultMap countResultMap = new ResultMap.Builder(config, "", Long.class, Collections.emptyList()).build();
        List<ResultMap> countResultMaps = Collections.singletonList(countResultMap);
        invocation.getArgs()[0] = newMappedStatement(ms, countBoundSql, countResultMaps, ms.getId() + "$count",
                Long.class);
        long totalItems = ((List<Long>) invocation.proceed()).get(0);

        List<?> items = (List<?>) resultSet;
        return new Paginator<>(items, new PageInfo((int) totalItems, pageable));
    }

    private static MappedStatement newMappedStatement(MappedStatement ms, BoundSql boundSql, List<ResultMap> resultMaps,
                                                      String id, Class<?> resultType) {
        SqlSource sqlSource = InterceptorSupport.createSqlSource(ms.getConfiguration(), boundSql);
        return InterceptorSupport.copyWith(ms, sqlSource, resultMaps, id, resultType);
    }

}
