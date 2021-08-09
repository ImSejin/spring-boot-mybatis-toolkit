package io.github.imsejin.mybatis.example.core.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.imsejin.mybatis.pagination.resolver.PageRequestResolver;
import io.github.imsejin.mybatis.typehandler.support.CodeEnumConverterFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Configuration
@RequiredArgsConstructor
class WebMvcConfig implements WebMvcConfigurer {

    private final ApplicationContext context;

    @Bean
    @Primary
    PageRequestResolver pageRequestResolver(ObjectMapper objectMapper) {
        return new PageRequestResolver(objectMapper);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.addAll(context.getBeansOfType(HandlerMethodArgumentResolver.class).values());

        log.debug("WebMvcConfigurer registered {} handler method argument resolver(s): {}",
                resolvers.size(), resolvers.stream().map(it -> it.getClass().getSimpleName()).collect(toList()));
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new CodeEnumConverterFactory<>());
    }

}
