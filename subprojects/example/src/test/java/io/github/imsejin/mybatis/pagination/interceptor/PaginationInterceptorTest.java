package io.github.imsejin.mybatis.pagination.interceptor;

import io.github.imsejin.mybatis.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PaginationInterceptorTest extends AbstractControllerTest {

    @Test
    void test() throws Exception {
        // given
        String page = "1";
        String size = "30";

        // when
        ResultActions actions = mockMvc.perform(
                get("/authors")
                        .param("page", page)
                        .param("size", size)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.pageInfo").isMap())
                .andExpect(jsonPath("$.pageInfo.page").value(page))
                .andExpect(jsonPath("$.pageInfo.size").value(size))
                .andExpect(jsonPath("$.items.*").isArray())
                .andExpect(jsonPath("$.items[-1:]").isArray()); // $.items[(@.length-1)]
    }

}
