package io.github.imsejin.mybatis.example.core.database.mybatis.config;

import io.github.imsejin.mybatis.example.Application;
import io.github.imsejin.mybatis.typehandler.support.DynamicCodeEnumTypeHandlerGenerator;
import io.github.imsejin.mybatis.typehandler.support.TypeHandlers;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DynamicCodeEnumTypeHandlerAutoConfig {

    @Bean
    @Primary
    Reflections reflections() {
        return new Reflections(Application.class);
    }

    @Bean
    @Primary
    TypeHandlers.TypeHandlerBuilder typeHandlerBuilder(Reflections reflections) throws ReflectiveOperationException {
        DynamicCodeEnumTypeHandlerGenerator generator = new DynamicCodeEnumTypeHandlerGenerator(reflections);

        return TypeHandlers.builder().add(generator.generateAll());
    }

}
