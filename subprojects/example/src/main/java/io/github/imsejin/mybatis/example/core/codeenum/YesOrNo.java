package io.github.imsejin.mybatis.example.core.codeenum;

import io.github.imsejin.mybatis.typehandler.model.CodeEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@Getter
@ToString
@RequiredArgsConstructor
public enum YesOrNo implements CodeEnum {

    YES("Y", "Yes"),

    NO("N", "No");

    private static final Map<String, YesOrNo> $CODE_LOOKUP = Arrays.stream(values())
            .collect(collectingAndThen(toMap(it -> it.code, it -> it), Collections::unmodifiableMap));

    private final String code;

    private final String codeName;

    public static boolean contains(String code) {
        return $CODE_LOOKUP.containsKey(code);
    }

    public static YesOrNo from(String code) {
        if (contains(code)) return $CODE_LOOKUP.get(code);
        throw new IllegalArgumentException(String.format("Enumeration 'YesOrNo' has no value '%s'", code));
    }

}
