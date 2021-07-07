package io.github.imsejin.mybatis.typehandler.model;

import com.fasterxml.jackson.annotation.JsonValue;
import io.github.imsejin.mybatis.typehandler.handler.CodeEnumTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Request parameter, Response result, Mapper에
 * 사용하기 위해 Enum이 구현해야 할 인터페이스.
 */
public interface CodeEnum {

    /**
     * Returns code.
     * <p>
     * This code is used in the following places.
     *
     * <ul>
     *     <li>
     *         <b>HTTP request parameter</b>:
     *         {@link io.github.imsejin.mybatis.typehandler.support.CodeEnumConverterFactory}
     *     </li>
     *     <li>
     *         <b>HTTP response body</b>:
     *         {@link JsonValue}
     *     </li>
     *     <li>
     *         <b>Mybatis mapper parameter</b>:
     *         {@link CodeEnumTypeHandler#setNonNullParameter(PreparedStatement, int, CodeEnum, JdbcType)}
     *     </li>
     *     <li>
     *         <b>Mybatis mapper result set</b>:
     *         {@link CodeEnumTypeHandler#getNullableResult(ResultSet, String)}
     *     </li>
     * </ul>
     *
     * @return code
     */
    @JsonValue
    String getCode();

    /**
     * Returns name of code.
     *
     * @return code name
     */
    String getCodeName();

}
