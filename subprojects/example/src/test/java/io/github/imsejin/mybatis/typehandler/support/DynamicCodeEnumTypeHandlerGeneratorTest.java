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

package io.github.imsejin.mybatis.typehandler.support;

import io.github.imsejin.mybatis.example.Application;
import io.github.imsejin.mybatis.example.core.codeenum.Status;
import io.github.imsejin.mybatis.example.core.codeenum.YesOrNo;
import io.github.imsejin.mybatis.example.core.model.KanCode;
import io.github.imsejin.mybatis.example.test.mapper.TestMapper;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class)
class DynamicCodeEnumTypeHandlerGeneratorTest {

    @Autowired
    private TestMapper mapper;

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    void testYesOrNo(YesOrNo yesOrNo) {
        assertThat(mapper.selectCodeByYesOrNo(yesOrNo))
                .isEqualTo(yesOrNo.getCode());
        assertThat(mapper.selectYesOrNoByCode(yesOrNo.getCode()))
                .isEqualTo(yesOrNo);
    }

    @ParameterizedTest
    @EnumSource(Status.class)
    void testStatus(Status status) {
        assertThat(mapper.selectCodeByStatus(status))
                .isEqualTo(status.getCode());
        assertThat(mapper.selectStatusByCode(status.getCode()))
                .isEqualTo(status);
    }

    @ParameterizedTest
    @ValueSource(strings = {"01000000", "01020000", "01020300", "01020304"})
    void testKanCode(String code) {
        // given
        KanCode kanCode = new KanCode(code);

        // when & then
        assertThat(mapper.selectCodeByKanCode(kanCode))
                .isEqualTo(kanCode.toString());
        assertThat(mapper.selectKanCodeByCode(kanCode.toString()))
                .isEqualTo(kanCode);
    }

    @RepeatedTest(5)
    void testUUID() {
        // given
        UUID uuid = UUID.randomUUID();

        // when & then
        assertThat(mapper.selectStringByUUID(uuid))
                .isEqualTo(uuid.toString());
        assertThat(mapper.selectUUIDByString(uuid.toString()))
                .isEqualTo(uuid);
    }

}
