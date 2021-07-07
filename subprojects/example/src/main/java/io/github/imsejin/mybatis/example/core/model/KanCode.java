/*
 * MIT License
 *
 * Copyright (c) 2021 Im Sejin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.imsejin.mybatis.example.core.model;

import com.fasterxml.jackson.annotation.JsonValue;
import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.ibatis.annotations.AutomapConstructor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * KAN(Korean Article Number) code
 * 한국에서 사용하는 국가표준코드로 제조, 물류, 유통에 사용한다.
 */
@Getter
@EqualsAndHashCode(of = "code")
public class KanCode {

    /**
     * 분류가 존재하지 않는다는 걸 의미하는 코드.
     */
    private static final String EMPTINESS = "00";

    /**
     * 대분류
     */
    private final String large;

    /**
     * 중분류
     */
    private final String medium;

    /**
     * 소분류
     */
    private final String small;

    /**
     * 세분류
     */
    private final String extraSmall;

    // "getCode()" is replaced with "toString()".
    @Getter(AccessLevel.NONE)
    private final String code;

    private final int depth;

    @AutomapConstructor
    public KanCode(String code) {
        validate0(code);

        this.large = code.substring(0, 2);
        this.medium = code.substring(2, 4);
        this.small = code.substring(4, 6);
        this.extraSmall = code.substring(6);

        // Finds depth of the kan code.
        int depth = 4;
        List<String> codes = Arrays.asList(large, medium, small, extraSmall);
        for (int i = 0; i < codes.size(); i++) {
            if (EMPTINESS.equals(codes.get(i))) {
                depth = i;
                break;
            }
        }

        this.depth = depth;
        this.code = code;
    }

    public KanCode(String large, String medium, String small, String extraSmall) {
        this(large + medium + small + extraSmall);
    }

    public static boolean validate(String code) {
        try {
            validate0(code);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void validate0(String code) {
        Asserts.that(code)
                .as("KanCode allows only 8 digits: '{0}'", code)
                .isNotNull().hasLengthOf(8).isNumeric();

        String large = code.substring(0, 2);
        String medium = code.substring(2, 4);
        String small = code.substring(4, 6);
        String extraSmall = code.substring(6);

        BiConsumer<String, String> throwException = (target, parent) -> {
            throw new IllegalArgumentException(
                    String.format("%s code must be '00' when %s code is '00'; code is '%s'", target, parent, code));
        };

        // 00000000
        if (large.equals(EMPTINESS)) {
            // 00010000
            if (!medium.equals(EMPTINESS)) throwException.accept("Medium", "large");
            // 00000100
            if (!small.equals(EMPTINESS)) throwException.accept("Small", "large");
            // 00000001
            if (!extraSmall.equals(EMPTINESS)) throwException.accept("Extra small", "large");
        }

        // 01000000
        if (medium.equals(EMPTINESS)) {
            // 00000100
            if (!small.equals(EMPTINESS)) throwException.accept("Small", "medium");
            // 00000001
            if (!extraSmall.equals(EMPTINESS)) throwException.accept("Extra small", "medium");
        }

        // 01020000
        if (small.equals(EMPTINESS)) {
            // 00000001
            if (!extraSmall.equals(EMPTINESS)) throwException.accept("Extra small", "small");
        }
    }

    @Override
    @JsonValue
    public String toString() {
        return this.code;
    }

    /**
     * Returns this is parent of a kan code.
     *
     * <pre><code>
     *     new KanCode("01020000").isParentOf(new KanCode("01020304")); // true
     *     new KanCode("01020304").isParentOf(new KanCode("01020000")); // false
     *     new KanCode("01020000").isParentOf(new KanCode("01020000")); // false
     * </code></pre>
     *
     * @param child child kan code
     */
    public boolean isParentOf(KanCode child) {
        if (this == child || this.equals(child)) return false;
        return child.code.startsWith(this.code.substring(0, this.depth * 2));
    }

    /**
     * Returns this is child of a kan code.
     *
     * <pre><code>
     *     new KanCode("01020000").isChildOf(new KanCode("01020304")); // false
     *     new KanCode("01020304").isChildOf(new KanCode("01020000")); // true
     *     new KanCode("01020000").isChildOf(new KanCode("01020000")); // false
     * </code></pre>
     *
     * @param parent parent kan code
     */
    public boolean isChildOf(KanCode parent) {
        if (this == parent || this.equals(parent)) return false;
        return this.code.startsWith(parent.code.substring(0, parent.depth * 2));
    }

    /**
     * Returns parent kan code of this.
     *
     * <pre><code>
     *     new KanCode("00000000").getParent(); // null
     *     new KanCode("01000000").getParent(); // new KanCode("00000000")
     *     new KanCode("01020000").getParent(); // new KanCode("01000000")
     *     new KanCode("01020300").getParent(); // new KanCode("01020000")
     *     new KanCode("01020304").getParent(); // new KanCode("01020300")
     * </code></pre>
     */
    @Nullable
    public KanCode getParent() {
        if (this.depth == 0) return null;

        String meaningfulCode = this.code.substring(0, this.depth * 2).replaceAll(".{2}$", EMPTINESS);
        return new KanCode(StringUtils.padEnd(8, meaningfulCode, "0"));
    }

}
