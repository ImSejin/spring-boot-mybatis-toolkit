package io.github.imsejin.mybatis.example.core.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.imsejin.mybatis.pagination.model.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PageRequestResolver implements HandlerMethodArgumentResolver {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == PageRequest.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Map<String, String> paramMap = new HashMap<>();

        for (Iterator<String> iter = webRequest.getParameterNames(); iter.hasNext(); ) {
            String paramName = iter.next();
            String param = webRequest.getParameter(paramName);

            paramMap.put(paramName, param);
        }

        String query = paramMap.get(PageRequest.QUERY_PROPERTY_NAME);
        paramMap.remove(PageRequest.QUERY_PROPERTY_NAME);
        PageRequest pageRequest = (PageRequest) objectMapper.convertValue(paramMap, parameter.getParameterType());

        if (StringUtils.hasText(query)) {
            Map<String, Object> queryMap = objectMapper.readValue(query, Map.class);
            pageRequest.setQuery(queryMap);
        }

        return pageRequest;
    }

}
