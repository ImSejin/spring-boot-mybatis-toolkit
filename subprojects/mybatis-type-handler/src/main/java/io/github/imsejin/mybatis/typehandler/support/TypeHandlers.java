package io.github.imsejin.mybatis.typehandler.support;

import net.jodah.typetools.TypeResolver;
import org.apache.ibatis.type.TypeHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * {@link Map} 그 자체를 bean으로 등록할 수 없기에
 * 핸들러 인스턴스들을 내부에 담아 값을 전달해 줄 컨테이너 객체를 만들었다.
 */
public class TypeHandlers {

    private final Map<Class<?>, TypeHandler<?>> typeHandlerMap;

    private TypeHandlers(Map<Class<?>, TypeHandler<?>> typeHandlerMap) {
        this.typeHandlerMap = typeHandlerMap;
    }

    public Map<Class<?>, TypeHandler<?>> get() {
        return this.typeHandlerMap;
    }

    public static TypeHandlerBuilder builder() {
        return new TypeHandlerBuilder();
    }

    public static class TypeHandlerBuilder {
        private final Map<Class<?>, TypeHandler<?>> typeHandlerMap = new HashMap<>();

        private TypeHandlerBuilder() {
        }

        public TypeHandlerBuilder add(Class<?> type, TypeHandler<?> typeHandler) {
            this.typeHandlerMap.put(type, typeHandler);
            return this;
        }

        @SuppressWarnings("unchecked")
        public <T> TypeHandlerBuilder add(Function<T, String> input, Function<String, T> output) {
            Class<T> type = (Class<T>) TypeResolver.resolveRawArguments(Function.class, input.getClass())[0];
            this.typeHandlerMap.put(type, TypeHandlerSupport.make(type, input, output));
            return this;
        }

        public TypeHandlerBuilder add(Map<Class<?>, TypeHandler<?>> typeHandlerMap) {
            this.typeHandlerMap.putAll(typeHandlerMap);
            return this;
        }

        public TypeHandlers build() {
            return new TypeHandlers(this.typeHandlerMap);
        }
    }

}
