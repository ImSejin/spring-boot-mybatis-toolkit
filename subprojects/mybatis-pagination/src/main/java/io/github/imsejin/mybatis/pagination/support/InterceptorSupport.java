package io.github.imsejin.mybatis.pagination.support;

import io.github.imsejin.mybatis.pagination.model.Pageable;
import io.github.imsejin.mybatis.pagination.model.Paginator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.type.TypeException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Supporter for MyBatis interceptor.
 *
 * @see Interceptor
 */
public final class InterceptorSupport {

    private InterceptorSupport() {
    }

    /**
     * Finds a method by mapped statement.
     *
     * @param ms mapped statement
     * @return mapper method
     * @see #findMethod(String)
     */
    public static Method findMethod(MappedStatement ms) {
        return findMethod(ms.getId());
    }

    /**
     * Finds a method by fully qualified method name.
     *
     * <p> There are some conditions of finding method.
     * This doesn't find a method, returns null.
     *
     * <ol>
     *     <li>Return type is {@link Paginator}?</li>
     *     <li>Has {@link Pageable} type or its implementation type in parameters?</li>
     * </ol>
     *
     * <pre><code>
     *     String fullName = "io.github.imsejin.mybatis.example.author.mapper.AuthorMapper.selectAll";
     *     Method selectAll = findMethod(fullName);
     * </code></pre>
     *
     * @param fullName fully qualified method name
     * @return mapper method
     * @throws ClassNotFoundException if full name is undefined
     */
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
            if (!method.getName().equals(methodName)) continue; // Trustworthy condition; it must be always false.
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
     * Returns {@link Pageable} instance from parameter.
     *
     * <p> This finds a {@link Pageable} instance
     * from parameter in {@link Invocation#getArgs()}.
     *
     * @param param mapper parameter
     * @return pageable instance
     * @throws IllegalArgumentException if pageable can not be found
     * @throws TypeException            if parameter is null
     * @see io.github.imsejin.mybatis.pagination.constant.MapperParameterType#from(BoundSql)
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

    /**
     * Parses select SQL query.
     *
     * @param selectSql select SQL query
     * @return {@link PlainSelect} instance
     * @throws JSQLParserException if failed to parse query
     * @throws TypeException       if query is not select query
     * @see net.sf.jsqlparser.parser.CCJSqlParser
     */
    public static PlainSelect parseSelect(String selectSql) {
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

}
