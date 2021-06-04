package io.github.imsejin.mybatis.typehandler.support;

import net.jodah.typetools.TypeResolver;
import org.apache.ibatis.type.TypeHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Easy builder for {@link TypeHandler}
 * <p>
 * Spring IOC Container doesn't allowed to register {@link java.lang.reflect.Array},
 * {@link java.util.Collection}, {@link Map}, etc as a bean. This class is created
 * for being as a container of instances of {@link TypeHandler}.
 */
public class TypeHandlers {

    private final Map<Class<?>, TypeHandler<?>> typeHandlerMap;

    private TypeHandlers(Map<Class<?>, TypeHandler<?>> typeHandlerMap) {
        this.typeHandlerMap = Collections.unmodifiableMap(typeHandlerMap);
    }

    /**
     * Returns unmodifiable map of {@link TypeHandler}.
     *
     * @return unmodifiable map of {@link TypeHandler}
     */
    public Map<Class<?>, TypeHandler<?>> get() {
        return this.typeHandlerMap;
    }

    /**
     * Creates builder for this.
     *
     * @return builder
     */
    public static TypeHandlerBuilder builder() {
        return new TypeHandlerBuilder(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Creates builder for this.
     *
     * @param classLoader class loader to load dynamically
     * @return builder
     */
    public static TypeHandlerBuilder builder(ClassLoader classLoader) {
        return new TypeHandlerBuilder(classLoader);
    }

    public static class TypeHandlerBuilder {
        private final Map<Class<?>, TypeHandler<?>> typeHandlerMap = new HashMap<>();
        private final ClassLoader classLoader;

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
         *             .add(generator.generateAll())
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

        /**
         * Returns a instance of {@link TypeHandlers}.
         *
         * @return instance of {@link TypeHandlers}
         */
        public TypeHandlers build() {
            return new TypeHandlers(this.typeHandlerMap);
        }
    }

}
