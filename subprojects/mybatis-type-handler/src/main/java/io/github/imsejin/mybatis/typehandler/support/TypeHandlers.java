package io.github.imsejin.mybatis.typehandler.support;

import net.jodah.typetools.TypeResolver;
import org.apache.ibatis.type.TypeHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

    public static TypeHandlerBuilder builder(ClassLoader classLoader) {
        return new TypeHandlerBuilder(classLoader);
    }

    public static class TypeHandlerBuilder {
        private final Map<Class<?>, TypeHandler<?>> typeHandlerMap = new HashMap<>();
        private final ClassLoader classLoader;

        private TypeHandlerBuilder() {
            this.classLoader = Thread.currentThread().getContextClassLoader();
        }

        private TypeHandlerBuilder(ClassLoader classLoader) {
            this.classLoader = Objects.requireNonNull(classLoader, "ClassLoader is not allowed to be null");
        }

        /**
         * Adds a type handler.
         *
         * <pre><code>
         *     TypeHandlers typeHandlers = TypeHandlers.builder()
         *             .add(new SampleTypeHandler())
         *             .build();
         * </code></pre>
         *
         * @param typeHandler type handler
         * @return this builder
         */
        public TypeHandlerBuilder add(TypeHandler<?> typeHandler) {
            Class<?> type = TypeResolver.resolveRawArguments(TypeHandler.class, typeHandler.getClass())[0];
            this.typeHandlerMap.put(type, typeHandler);
            return this;
        }

        /**
         * Adds a type handler with in/out functions.
         *
         * <pre><code>
         *     TypeHandlers typeHandlers = TypeHandlers.builder()
         *             .add(UUID::toString, UUID::fromString)
         *             .build();
         * </code></pre>
         *
         * @param input  to string function
         * @param output to model function
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        public <T> TypeHandlerBuilder add(Function<T, String> input, Function<String, T> output) {
            Class<T> type = (Class<T>) TypeResolver.resolveRawArguments(Function.class, input.getClass())[0];
            this.typeHandlerMap.put(type, TypeHandlerFactory.create(type, input, output, this.classLoader));
            return this;
        }

        /**
         * Adds type handlers.
         *
         * <pre><code>
         *     String basePackage = "io.github.imsejin.mybatis.example";
         *     DynamicCodeEnumTypeHandlerGenerator generator
         *         = new DynamicCodeEnumTypeHandlerGenerator(basePackage);
         *
         *     TypeHandlers typeHandlers = TypeHandlers.builder()
         *             .add(generator.findTypeHandlers())
         *             .build();
         * </code></pre>
         *
         * @param typeHandlerMap type handler map
         * @return this builder
         */
        public TypeHandlerBuilder add(Map<Class<?>, TypeHandler<?>> typeHandlerMap) {
            this.typeHandlerMap.putAll(typeHandlerMap);
            return this;
        }

        public TypeHandlers build() {
            return new TypeHandlers(this.typeHandlerMap);
        }
    }

}
