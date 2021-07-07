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

import io.github.imsejin.mybatis.AbstractControllerTest;
import io.github.imsejin.mybatis.example.Application;
import io.github.imsejin.mybatis.example.core.codeenum.Status;
import io.github.imsejin.mybatis.example.core.codeenum.YesOrNo;
import io.github.imsejin.mybatis.typehandler.controller.CodeEnumController;
import io.github.imsejin.mybatis.typehandler.model.CodeEnum;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {
        Application.class,
        CodeEnumController.class,
})
class CodeEnumConverterFactoryTest extends AbstractControllerTest {

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    void testYesOrNo(CodeEnum yesOrNo) throws Exception {
        // when
        ResultActions actions = mockMvc.perform(
                get("/codeenum/{className}/code/{yesOrNo}", yesOrNo.getClass().getSimpleName().toLowerCase(), yesOrNo.getCode())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        actions.andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$").isString())
                .andExpect(jsonPath("$").value(yesOrNo.getCodeName()));
    }

    @ParameterizedTest
    @EnumSource(Status.class)
    void testStatus(CodeEnum status) throws Exception {
        // when
        ResultActions actions = mockMvc.perform(
                get("/codeenum/{className}/code/{status}", status.getClass().getSimpleName().toLowerCase(), status.getCode())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        actions.andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$").isString())
                .andExpect(jsonPath("$").value(status.getCodeName()));
    }

}
