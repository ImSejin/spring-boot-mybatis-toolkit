package io.github.imsejin.mybatis.pagination.support;

import io.github.imsejin.mybatis.pagination.model.Pageable;
import io.github.imsejin.mybatis.pagination.model.Paginator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public final class InterceptorSupport {

    private InterceptorSupport() {
    }

    public static Method findMethod(MappedStatement ms) {
        return findMethod(ms.getId());
    }

    public static Method findMethod(String fullName) {
        final int index = fullName.lastIndexOf('.');
        String className = fullName.substring(0, index);
        String methodName = fullName.substring(index + 1);

        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.getName().equals(methodName)) continue;
            if (method.getReturnType() != Paginator.class) continue;
            if (Arrays.stream(method.getParameterTypes()).noneMatch(Pageable.class::isAssignableFrom)) continue;

            return method;
        }

        return null;
    }

    public static Class<?> findResultMapType(MappedStatement ms) {
        for (ResultMap resultMap : ms.getResultMaps()) {
            return resultMap.getType();
        }

        throw new TypeException("Not found result type");
    }

    /**
     * Returns pageable instance from parameter.
     *
     * <p> Element in index 1 of {@link Invocation#getArgs()} is the
     * argument of mapper's method. When parameter length of its method is 1,
     * the type of element is the type of argument. When greater than or equal to 2,
     * the type of elements is {@link MapperMethod.ParamMap}.
     *
     * @param param mapper parameter
     * @return pageable instance
     */
    public static Pageable getPageableFromParam(Object param) {
        if (param instanceof Pageable) {
            return (Pageable) param;

        } else if (param instanceof MapperMethod.ParamMap) {
            MapperMethod.ParamMap<?> paramMap = (MapperMethod.ParamMap<?>) param;
            for (Object value : paramMap.values()) {
                if (!Pageable.class.isAssignableFrom(value.getClass())) continue;
                return (Pageable) value;
            }

            throw new IllegalArgumentException("Not found parameter: " + Pageable.class.getName());
        } else {
            throw new TypeException("Unsupported parameter type: " + param.getClass());
        }
    }

    public static PlainSelect newSelect(String selectSql) {
        Statement statement;
        try {
            statement = CCJSqlParserUtil.parse(selectSql);
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }

        if (!(statement instanceof Select)) {
            throw new TypeException("Not SELECT statement: " + statement);
        }

        Select select = (Select) statement;
        if (!(select.getSelectBody() instanceof PlainSelect)) {
            throw new TypeException("Not SELECT statement: " + select);
        }

        return select.getSelectBody(PlainSelect.class);
    }

    public static int getNumOfMappings(String sql) {
        int frequency = 0;

        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') frequency++;
        }

        return frequency;
    }

    public static int[] getPositionsOfMapping(String sql) {
        int numOfMappings = getNumOfMappings(sql);
        if (numOfMappings == 0) return new int[0];

        return IntStream.range(0, numOfMappings).toArray();
    }

    public static int countOccurrencesOf(String str, String sub) {
        if (str == null || str.isEmpty() || sub == null || sub.isEmpty()) return 0;

        int count = 0;
        int pos = 0;
        int idx;
        while ((idx = str.indexOf(sub, pos)) != -1) {
            ++count;
            pos = idx + sub.length();
        }
        return count;
    }

    public static SqlSource createSqlSource(Configuration config, BoundSql boundSql) {
        return new StaticSqlSource(config, boundSql.getSql(), boundSql.getParameterMappings());
    }

    public static SqlSource createSqlSource(Configuration config, String sql, List<ParameterMapping> mappings) {
        return new StaticSqlSource(config, sql, mappings);
    }

    public static MappedStatement copyWith(MappedStatement origin, SqlSource sqlSource, String id, Class<?> resultType) {
        ResultMap countResultMap = new ResultMap
                .Builder(origin.getConfiguration(), id, resultType, Collections.emptyList())
                .build();

        List<ResultMap> resultMaps = Collections.singletonList(countResultMap);

        return new MappedStatement.Builder(origin.getConfiguration(), id,
                sqlSource, origin.getSqlCommandType())
                .resource(origin.getResource())
                .parameterMap(origin.getParameterMap())
                .resultMaps(resultMaps)
                .fetchSize(origin.getFetchSize())
                .timeout(origin.getTimeout())
                .statementType(origin.getStatementType())
                .resultSetType(origin.getResultSetType())
                .cache(origin.getCache())
                .flushCacheRequired(origin.isFlushCacheRequired())
                .useCache(true)
                .resultOrdered(origin.isResultOrdered())
                .keyGenerator(origin.getKeyGenerator())
                .keyColumn(origin.getKeyColumns() == null ? null : String.join(",", origin.getKeyColumns()))
                .keyProperty(origin.getKeyProperties() == null ? null : String.join(",", origin.getKeyProperties()))
                .databaseId(origin.getDatabaseId())
                .lang(origin.getLang())
                .resultSets(origin.getResultSets() == null ? null : String.join(",", origin.getResultSets()))
                .build();
    }

    public static BoundSql copyWith(BoundSql origin, Configuration config, String sql) {
        return new BoundSql(config, sql, origin.getParameterMappings(), origin.getParameterObject());
    }

    public static BoundSql copyWith(BoundSql origin, Configuration config, String sql, List<ParameterMapping> mappings) {
        return new BoundSql(config, sql, mappings, origin.getParameterObject());
    }

}
