package io.github.imsejin.mybatis.example.core.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final Reflections reflections;

    private final ApplicationContext context;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        Set<Class<? extends HandlerMethodArgumentResolver>> resolverTypes = reflections
                .getSubTypesOf(HandlerMethodArgumentResolver.class).stream()
                .filter(it -> !Modifier.isAbstract(it.getModifiers())).collect(toSet());

        resolverTypes.stream().map(context::getBean).forEach(resolvers::add);

        log.debug("WebMvcConfig registered {} handler method argument resolver(s): {}",
                resolverTypes.size(), resolverTypes.stream().map(Class::getSimpleName).collect(toList()));
    }

}
