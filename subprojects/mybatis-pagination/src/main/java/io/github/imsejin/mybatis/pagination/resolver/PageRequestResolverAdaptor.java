package io.github.imsejin.mybatis.pagination.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.imsejin.mybatis.pagination.model.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
public abstract class PageRequestResolverAdaptor implements HandlerMethodArgumentResolver {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return PageRequest.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Map<String, String> paramMap = new HashMap<>();

        // Gets all request parameters.
        for (Iterator<String> iter = webRequest.getParameterNames(); iter.hasNext(); ) {
            String paramName = iter.next();
            String param = webRequest.getParameter(paramName);

            paramMap.put(paramName, param);
        }

        // Separates parameter "PageRequest.QUERY_PROPERTY_NAME" from others.
        String query = paramMap.get(PageRequest.QUERY_PROPERTY_NAME);
        paramMap.remove(PageRequest.QUERY_PROPERTY_NAME);

        // Converts "Map" instance to "PageRequest" instance.
        PageRequest pageRequest = objectMapper.convertValue(paramMap, PageRequest.class);

        // Converts JSON format object to "Map" instance.
        if (StringUtils.hasText(query)) {
            Map<String, Object> queryMap = objectMapper.readValue(query, Map.class);
            pageRequest.setQuery(queryMap);
        }

        return pageRequest;
    }

}
