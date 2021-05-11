package io.github.imsejin.mybatis.example.core.database.mybatis.config;

import io.github.imsejin.mybatis.example.Application;
import io.github.imsejin.mybatis.typehandler.config.DynamicCodeEnumTypeHandlerAutoConfigurer;
import io.github.imsejin.mybatis.typehandler.model.CodeEnum;
import org.apache.ibatis.type.TypeHandler;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DynamicCodeEnumTypeHandlerAutoConfig {

    @Bean
    @Primary
    Reflections reflections() {
        return new Reflections(Application.class);
    }

    @Bean
    @Primary
    CodeEnumTypeHandlers codeEnumTypeHandlers(Reflections reflections) throws ReflectiveOperationException {
        DynamicCodeEnumTypeHandlerAutoConfigurer configurer = new DynamicCodeEnumTypeHandlerAutoConfigurer(reflections);
        Map<Class<? extends CodeEnum>, TypeHandler<?>> typeHandlerMap = configurer.findTypeHandlers();

        return new CodeEnumTypeHandlers(typeHandlerMap);
    }

    /**
     * {@link Map} 그 자체를 bean으로 등록할 수 없기에
     * 핸들러 인스턴스들을 내부에 담아 값을 전달해 줄 컨테이너 객체를 만들었다.
     */
    public static class CodeEnumTypeHandlers {
        private final Map<Class<? extends CodeEnum>, TypeHandler<?>> typeHandlerMap;

        public CodeEnumTypeHandlers(Map<Class<? extends CodeEnum>, TypeHandler<?>> typeHandlerMap) {
            this.typeHandlerMap = Collections.unmodifiableMap(typeHandlerMap);
        }

        public Map<Class<? extends CodeEnum>, TypeHandler<?>> get() {
            return new HashMap<>(this.typeHandlerMap);
        }
    }

}
