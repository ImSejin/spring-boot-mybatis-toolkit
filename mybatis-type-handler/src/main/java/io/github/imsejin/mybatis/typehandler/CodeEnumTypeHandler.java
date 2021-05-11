package io.github.imsejin.mybatis.typehandler;

import io.github.imsejin.mybatis.typehandler.config.DynamicCodeEnumTypeHandlerAutoConfigurer;
import io.github.imsejin.mybatis.typehandler.model.CodeEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Adaptor of {@link TypeHandler} resolving implementations of {@link CodeEnum}.
 *
 * @see DynamicCodeEnumTypeHandlerAutoConfigurer
 */
public abstract class CodeEnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<CodeEnum> {

    private final Class<E> type;
    private final CodeEnum[] enumConstants;

    public CodeEnumTypeHandler(Class<E> type) {
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        if (!type.isEnum()) {
            String message = String.format("Type must be enum: '%s'", type.getName());
            throw new IllegalArgumentException(message);
        }

        // This is an array, if type is enum and null if not.
        this.enumConstants = (CodeEnum[]) type.getEnumConstants();

        if (this.enumConstants.length == 0) {
            String message = String.format("Enum type must have at least one constant: '%s'", type.getName());
            throw new IllegalArgumentException(message);
        }
    }

    private CodeEnum getCodeEnum(String code) {
        try {
            for (CodeEnum codeEnum : this.enumConstants) {
                if (codeEnum.getCode().equals(code)) return codeEnum;
            }
            return null;
        } catch (Exception e) {
            throw new TypeException(String.format("Cannot make enum object '%s'", type), e);
        }
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