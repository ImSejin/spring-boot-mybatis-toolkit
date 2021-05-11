package io.github.imsejin.mybatis.example.core.web;

import io.github.imsejin.mybatis.example.Application;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApplicationContext context;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        Reflections reflections = new Reflections(Application.class.getPackage().getName());
        Set<Class<? extends HandlerMethodArgumentResolver>> resolverTypes = reflections
                .getSubTypesOf(HandlerMethodArgumentResolver.class);

        resolverTypes.forEach(it -> resolvers.add(context.getBean(it)));

        log.info("WebMvcConfig registered {} resolver(s) for handler method argument", resolverTypes.size());
    }

}
