package io.github.imsejin.mybatis.typehandler.support;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.Function;

import static net.bytebuddy.matcher.ElementMatchers.*;

public final class TypeHandlerSupport {

    private TypeHandlerSupport() {
    }

    @SuppressWarnings("unchecked")
    public static <T> BaseTypeHandler<T> make(Class<T> type, Function<T, String> input, Function<String, T> output)
            throws ReflectiveOperationException {
        ClassLoader classLoader = TypeHandlerSupport.class.getClassLoader();

        // Creates BaseTypeHandler<T> as a type.
        TypeDescription.Generic baseTypeHandler = TypeDescription.Generic.Builder.parameterizedType(BaseTypeHandler.class, type)
                .build();

        Class<? extends BaseTypeHandler<T>> dynamicType = (Class<? extends BaseTypeHandler<T>>) new ByteBuddy()
                .subclass(baseTypeHandler)
                /*
                 * @Override
                 * public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType)
                 *         throws SQLException {
                 *     if (jdbcType == null) {
                 *         ps.setString(i, toString.apply(parameter));
                 *     } else {
                 *         ps.setObject(i, toString.apply(parameter), jdbcType.TYPE_CODE); // see r3589
                 *     }
                 * }
                 */
                .method(named("setNonNullParameter").and(isAbstract())
                        .and(takesArguments(PreparedStatement.class, int.class, type, JdbcType.class))
                        .and(returns(void.class)))
                .intercept(InvocationHandlerAdapter.of((proxy, method, args) -> {
                    PreparedStatement ps = (PreparedStatement) args[0];
                    int i = (int) args[1];
                    T parameter = (T) args[2];
                    JdbcType jdbcType = (JdbcType) args[3];

                    if (jdbcType == null) {
                        ps.setString(i, input.apply(parameter));
                    } else {
                        ps.setObject(i, input.apply(parameter), jdbcType.TYPE_CODE); // see r3589
                    }

                    return null;
                }))
                /*
                 * @Override
                 * public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
                 *     String code = rs.getString(columnName);
                 *     return code == null ? null : outputConverter.apply(code);
                 * }
                 */
                .method(named("getNullableResult").and(isAbstract())
                        .and(takesArguments(ResultSet.class, String.class))
                        .and(returns(type)))
                .intercept(InvocationHandlerAdapter.of((proxy, method, args) -> {
                    ResultSet rs = (ResultSet) args[0];
                    String columnName = (String) args[1];

                    String code = rs.getString(columnName);
                    return code == null ? null : output.apply(code);
                }))
                /*
                 * @Override
                 * public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
                 *     String code = rs.getString(columnIndex);
                 *     return code == null ? null : outputConverter.apply(code);
                 * }
                 */
                .method(named("getNullableResult").and(isAbstract())
                        .and(takesArguments(ResultSet.class, int.class))
                        .and(returns(type)))
                .intercept(InvocationHandlerAdapter.of((proxy, method, args) -> {
                    ResultSet rs = (ResultSet) args[0];
                    int columnIndex = (int) args[1];

                    String code = rs.getString(columnIndex);
                    return code == null ? null : output.apply(code);
                }))
                /*
                 * @Override
                 * public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
                 *     String code = cs.getString(columnIndex);
                 *     return code == null ? null : outputConverter.apply(code);
                 * }
                 */
                .method(named("getNullableResult").and(isAbstract())
                        .and(takesArguments(CallableStatement.class, int.class))
                        .and(returns(void.class)))
                .intercept(InvocationHandlerAdapter.of((proxy, method, args) -> {
                    CallableStatement cs = (CallableStatement) args[0];
                    int columnIndex = (int) args[1];

                    String code = cs.getString(columnIndex);
                    return code == null ? null : output.apply(code);
                })).make().load(classLoader).getLoaded();

        return dynamicType.getConstructor().newInstance();
    }

}
