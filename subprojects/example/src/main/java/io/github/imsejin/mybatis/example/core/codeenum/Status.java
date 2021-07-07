package io.github.imsejin.mybatis.example.core.codeenum;

import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.mybatis.typehandler.model.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@Getter
@ToString
@RequiredArgsConstructor
public enum Status implements CodeEnum {

    /**
     * 검수 대기
     */
    WAITING("STATUS_WAIT", "검수대기"),

    /**
     * 검수 완료
     */
    COMPLETE("STATUS_COM", "검수완료"),

    /**
     * 재검수 대기
     */
    REWAITING("STATUS_REWAIT", "재검수대기"),

    /**
     * 검수 반려
     */
    RETURNED("STATUS_RTN", "검수반려");

    private static final Map<String, Status> $CODE_LOOKUP = EnumSet.allOf(Status.class).stream()
            .collect(collectingAndThen(toMap(it -> it.code, it -> it), Collections::unmodifiableMap));

    private final String code;
    private final String codeName;

    public static boolean contains(String code) {
        return $CODE_LOOKUP.containsKey(code);
    }

    public static Status from(String code) {
        Asserts.that(contains(code)).as("Enumeration 'Status' has no value '{0}'", code).isTrue();
        return $CODE_LOOKUP.get(code);
    }

}
