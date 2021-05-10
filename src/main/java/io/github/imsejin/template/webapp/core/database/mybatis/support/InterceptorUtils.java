package io.github.imsejin.template.webapp.core.database.mybatis.support;

import io.github.imsejin.template.webapp.core.database.mybatis.model.pagination.Page;
import io.github.imsejin.template.webapp.core.database.mybatis.model.pagination.PageRequest;
import io.github.imsejin.template.webapp.core.database.mybatis.model.pagination.Pageable;
import io.github.imsejin.template.webapp.core.database.mybatis.model.pagination.Paginator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.type.TypeException;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.IntStream;

public final class InterceptorUtils {

    private InterceptorUtils() {
    }

    @Nullable
    public static Method findMethod(MappedStatement ms) {
        return findMethod(ms.getId());
    }

    @Nullable
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

    /**
     * Returns pageable instance from parameter.
     *
     * <p> Element in index 1 of {@link Invocation#getArgs()} is the
     * argument of mapper's method. When parameter length of its method is 1,
     * the type of element is the type of argument. When greater than or equal to 2,
     * the type of elements is {@link org.apache.ibatis.binding.MapperMethod.ParamMap}.
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

}
