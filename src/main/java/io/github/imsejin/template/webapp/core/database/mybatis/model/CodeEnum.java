package io.github.imsejin.template.webapp.core.database.mybatis.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Request parameter, Response result, Mapper에
 * 사용하기 위해 Enum이 구현해야 할 인터페이스.
 */
public interface CodeEnum {

    /**
     * Response result에 사용될 코드.
     */
    @JsonValue
    String getCode();

    String getCodeName();

}
