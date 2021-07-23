package io.github.imsejin.mybatis.pagination.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.imsejin.mybatis.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PaginationInterceptorTest extends AbstractControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void test0() throws Exception {
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

    @Test
    void test1() throws Exception {
        // given
        String page = "1";
        String size = "10";
        Map<String, Object> query = new HashMap<>();
        query.put("name", "Bill Gates");
        query.put("country", "United States America");
        query.put("startDate", "1955-01-01");
        query.put("endDate", "1955-12-31");

        // when
        ResultActions actions = mockMvc.perform(
                get("/authors")
                        .param("page", page)
                        .param("size", size)
                        .param("query", objectMapper.writeValueAsString(query))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        Pattern pattern = Pattern.compile("[\\da-z]{8}-[\\da-z]{4}-[\\da-z]{4}-[\\da-z]{4}-[\\da-z]{12}");
        actions
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.pageInfo").isMap())
                .andExpect(jsonPath("$.pageInfo.page").value(page))
                .andExpect(jsonPath("$.pageInfo.size").value(size))
                .andExpect(jsonPath("$.pageInfo.totalItems").value(1))
                .andExpect(jsonPath("$.items.*").isArray())
                .andExpect(jsonPath("$.items[0].name").value(query.get("name")))
                .andExpect(jsonPath("$.items[0].country").value(query.get("country")))
                .andExpect(jsonPath("$.items[0].birthdate").value("1955-10-28"))
                .andExpect(jsonPath("$.items[0].uuid").value(matchesPattern(pattern)));
    }

    @Test
    void test2() throws Exception {
        // given
        String page = "1";
        String size = "10";
        Map<String, Object> query = new HashMap<>();
        query.put("country", "Colombia");
        String number = IntStream.range(2, 10).mapToObj(String::valueOf).reduce((acc, cur) -> acc + ',' + cur).get();
        query.put("number", number);

        // when
        ResultActions actions = mockMvc.perform(
                get("/authors")
                        .param("page", page)
                        .param("size", size)
                        .param("query", objectMapper.writeValueAsString(query))
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
