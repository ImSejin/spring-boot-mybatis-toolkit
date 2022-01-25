package io.github.imsejin.mybatis.typehandler.handler;

import io.github.imsejin.mybatis.typehandler.model.CodeEnum;
import io.github.imsejin.mybatis.typehandler.support.DynamicCodeEnumTypeHandlerGenerator;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Adaptor of {@link TypeHandler} resolving implementations of {@link CodeEnum}.
 *
 * @see DynamicCodeEnumTypeHandlerGenerator
 */
public abstract class CodeEnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<CodeEnum> {

    private final Class<E> type;
    private final Map<String, CodeEnum> cache;

    public CodeEnumTypeHandler(Class<E> type) {
        this.type = Objects.requireNonNull(type, "Type should not be null");
        if (!type.isEnum()) {
            String message = String.format("Type must be enum: '%s'", type.getName());
            throw new IllegalArgumentException(message);
        }

        // This is an array, if type is enum or else null.
        CodeEnum[] enumConstants = (CodeEnum[]) type.getEnumConstants();

        if (enumConstants.length == 0) {
            String message = String.format("Enum type must have at least one constant: '%s'", type.getName());
            throw new IllegalArgumentException(message);
        }

        // Creates a cache for fast lookup.
        Map<String, CodeEnum> cache = new HashMap<>();
        for (CodeEnum codeEnum : enumConstants) {
            cache.put(codeEnum.getCode(), codeEnum);
        }
        this.cache = Collections.unmodifiableMap(cache);
    }

    private CodeEnum getCodeEnum(String code) {
        CodeEnum codeEnum = this.cache.get(code);

        if (codeEnum != null) return codeEnum;
        throw new TypeException(String.format("Enumeration '%s' has no code '%s'", this.type.getSimpleName(), code));
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, CodeEnum parameter, JdbcType jdbcType)
            throws SQLException {
        if (jdbcType == null) {
            ps.setString(i, parameter.getCode());
        } else {
            ps.setObject(i, parameter.getCode(), jdbcType.TYPE_CODE); // see r3589
        }
    }

    @Override
    public CodeEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String code = rs.getString(columnName);
        return code == null ? null : getCodeEnum(code);
    }

    @Override
    public CodeEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return code == null ? null : getCodeEnum(code);
    }

    @Override
    public CodeEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return code == null ? null : getCodeEnum(code);
    }

}
