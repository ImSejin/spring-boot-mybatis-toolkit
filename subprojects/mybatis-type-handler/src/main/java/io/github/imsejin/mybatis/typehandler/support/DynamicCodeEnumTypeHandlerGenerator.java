/*
 * MIT License
 *
 * Copyright (c) 2021 Im Sejin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.imsejin.mybatis.typehandler.support;

import io.github.imsejin.mybatis.typehandler.handler.CodeEnumTypeHandler;
import io.github.imsejin.mybatis.typehandler.model.CodeEnum;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.implementation.MethodCall;
import org.apache.ibatis.type.TypeHandler;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Dynamic generator for subclasses of {@link CodeEnumTypeHandler}
 * <p>
 * This finds all implementations of {@link CodeEnum} and
 * dynamically generates and instantiates the types that extend {@link CodeEnumTypeHandler}.
 * <p>
 * If you remove {@code abstract} keyword on {@link CodeEnumTypeHandler} and instantiates,
 * you can register. But it is not registration for each instance.
 * MyBatis internally determines the type of instance and adopts 1:1 mapping policy within
 * {@code SqlSessionFactoryBean} in which one {@link TypeHandler} is mapped to one type.
 * Therefore, only one implementation of {@link CodeEnum} can be registered using the following code.
 *
 * <pre><code>
 * Set&lt;Class&lt;? extends CodeEnum>> subclasses = reflections.getSubTypesOf(CodeEnum.class);
 * List&lt;TypeHandler&lt;?>> typeHandlers = subclasses.stream()
 *         .map(CodeEnumTypeHandler::new)
 *         .collect(Collectors.toList());
 * </code></pre>
 * I've tried with anonymous classes, but they don't seem to be supported.
 * Using {@link ByteBuddy}, this dynamically defines types of {@link CodeEnumTypeHandler} and
 * instantiates like the following code.
 *
 * <pre><code>
 * public enum CodeEnumImpl implementations CodeEnum { ... }
 *
 * public class DynamicTypeHandler extends CodeEnumTypeHandler&lt;CodeEnumImpl> {
 *     public DynamicTypeHandler() {
 *         super(CodeEnumImpl.class);
 *     }
 * }
 * </code></pre>
 *
 * @see Reflections
 * @see ByteBuddy
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DynamicCodeEnumTypeHandlerGenerator {

    private final Reflections reflections;

    private final ClassLoader classLoader;

    public DynamicCodeEnumTypeHandlerGenerator(Class<?> basePackageClass) {
        this(basePackageClass.getPackage().getName());
    }

    public DynamicCodeEnumTypeHandlerGenerator(Class<?> basePackageClass, ClassLoader classLoader) {
        this(basePackageClass.getPackage().getName(), classLoader);
    }

    public DynamicCodeEnumTypeHandlerGenerator(String basePackage) {
        this(new Reflections(Objects.requireNonNull(basePackage, "Base package is not allowed to be null")));
    }

    public DynamicCodeEnumTypeHandlerGenerator(String basePackage, ClassLoader classLoader) {
        this(new Reflections(Objects.requireNonNull(basePackage, "Base package is not allowed to be null")),
                classLoader);
    }

    public DynamicCodeEnumTypeHandlerGenerator(Reflections reflections) {
        this(reflections, Thread.currentThread().getContextClassLoader());
    }

    public DynamicCodeEnumTypeHandlerGenerator(Reflections reflections, ClassLoader classLoader) {
        this.reflections = Objects.requireNonNull(reflections, "Reflections is not allowed to be null");
        this.classLoader = Objects.requireNonNull(classLoader, "ClassLoader is not allowed to be null");
    }

    /**
     * Finds all subclasses of {@link CodeEnumTypeHandler} and instantiates them.
     *
     * @return all subclasses of {@link CodeEnumTypeHandler}
     * @throws ReflectiveOperationException if failed to instantiate class
     */
    public Map<Class<?>, TypeHandler<?>> generateAll() throws ReflectiveOperationException {
        return generate0(null);
    }

    /**
     * Finds some subclasses of {@link CodeEnumTypeHandler} and instantiates them.
     *
     * <pre><code>
     *     generate(Policy.INCLUDE);                    // none of A, B and C
     *     generate(Policy.INCLUDE, A.class, B.class);  // A and B of A, B and C
     *     generate(Policy.EXCLUDE);                    // all
     *     generate(Policy.EXCLUDE, A.class, B.class);  // C of A, B and C
     * </code></pre>
     *
     * @param policy  choice policy
     * @param classes which classes are chosen?
     * @return some subclasses of {@link CodeEnumTypeHandler}
     * @throws ReflectiveOperationException if failed to instantiate class
     */
    public <T extends Enum & CodeEnum> Map<Class<?>, TypeHandler<?>> generate(Policy policy, Class<T>... classes)
            throws ReflectiveOperationException {
        if (policy == Policy.INCLUDE && classes.length == 0) return Collections.emptyMap();
        return generate0(Objects.requireNonNull(policy, "Policy is not allowed to be null"), classes);
    }

    private <T extends Enum & CodeEnum> Map<Class<?>, TypeHandler<?>> generate0(Policy policy, Class<T>... classes)
            throws ReflectiveOperationException {
        Set<Class<? extends CodeEnum>> subclasses = this.reflections.getSubTypesOf(CodeEnum.class);

        // "java.lang.Class"를 파라미터로 갖는 "CodeEnumTypeHandler"의 생성자.
        Class<CodeEnumTypeHandler> superType = CodeEnumTypeHandler.class;
        Constructor<CodeEnumTypeHandler> superConstructor = superType.getConstructor(Class.class);

        List<Class<T>> classList = Arrays.asList(classes);
        Map<Class<?>, TypeHandler<?>> typeHandlerMap = new HashMap<>();
        for (Class<? extends CodeEnum> type : subclasses) {
            /*
             * "Enum"이 아닌 타입이거나, abstract method를 갖고 있는 "Enum"의 경우
             * 각 constant가 anonymous class로 생성된다.
             * 이런 타입으로 "CodeEnumTypeHandler"를 생성할 수 없다.
             */
            if (!Enum.class.isAssignableFrom(type) || type.isAnonymousClass()) continue;

            if (policy == Policy.INCLUDE && !classList.contains(type)) continue;
            else if (policy == Policy.EXCLUDE && classList.contains(type)) continue;

            // "CodeEnumTypeHandler"를 상속하는 동적 타입을 생성한다.
            Class<? extends CodeEnumTypeHandler> dynamicType = new ByteBuddy().subclass(superType)
                    .defineConstructor(Visibility.PUBLIC).intercept(MethodCall.invoke(superConstructor).with(type))
                    .make().load(this.classLoader).getLoaded();

            // 기본 생성자로 동적 타입의 인스턴스를 생성한다.
            Constructor<? extends CodeEnumTypeHandler> constructor = dynamicType.getConstructor();
            CodeEnumTypeHandler<?> dynamicTypeHandler = constructor.newInstance();

            typeHandlerMap.put(type, dynamicTypeHandler);
        }

        return typeHandlerMap;
    }

    public enum Policy {
        INCLUDE, EXCLUDE
    }

}
