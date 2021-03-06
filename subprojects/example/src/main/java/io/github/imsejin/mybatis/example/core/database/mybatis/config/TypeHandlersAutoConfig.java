package io.github.imsejin.mybatis.example.core.database.mybatis.config;

import io.github.imsejin.mybatis.example.Application;
import io.github.imsejin.mybatis.example.core.model.KanCode;
import io.github.imsejin.mybatis.typehandler.support.DynamicCodeEnumTypeHandlerGenerator;
import io.github.imsejin.mybatis.typehandler.support.TypeHandlers;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.UUID;

@Configuration
class TypeHandlersAutoConfig {

    @Bean
    @Primary
    static Reflections reflections() {
        return new Reflections(Application.class);
    }

    @Bean
    @Primary
    static TypeHandlers typeHandlers(Reflections reflections) throws ReflectiveOperationException {
        DynamicCodeEnumTypeHandlerGenerator generator = new DynamicCodeEnumTypeHandlerGenerator(reflections);

        return TypeHandlers.builder()
                .add(generator.generateAll()) // io.github.imsejin.mybatis.typehandler.model.CodeEnum
                .add(UUID::toString, UUID::fromString) // java.util.UUID
                .add(KanCode::toString, KanCode::new) // io.github.imsejin.mybatis.example.core.model.KanCode
                .build();
    }

}
