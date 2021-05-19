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
 * {@link CodeEnum}의 모든 구현체를 찾아, {@link CodeEnumTypeHandler}를 상속하는
 * 타입을 동적으로 생성 및 인스턴스화한다.
 *
 * <p> {@link CodeEnumTypeHandler}의 abstract 키워드를 제거하여 일반 생성자로 아래와 같이 생성하면
 * 등록이 되긴 하나, {@code SqlSessionFactoryBean.setTypeHandlers(TypeHandler[])}가
 * 각각의 핸들러 인스턴스를 등록하는 것이 아니다.
 *
 * <p> 내부적으로 핸들러 인스턴스의 타입을 판단하여 {@code SqlSessionFactoryBean} 내에서
 * 하나의 {@link TypeHandler}가 하나의 타입에만 적용되는 1:1 매핑 방식을 채용한다.
 * 따라서 아래와 같은 방식으로는 {@link CodeEnum}의 구현체를 오직 1개만 등록할 수 있다.
 *
 * <pre><code>
 * Set<Class<? extends CodeEnum>> subclasses = reflections.getSubTypesOf(CodeEnum.class);
 * List<TypeHandler<?>> typeHandlers = new ArrayList<>();
 *
 * for (Class<? extends CodeEnum> type : subclasses) {
 *     typeHandlers.add(new CodeEnumTypeHandler(type));
 * }
 * </code></pre>
 *
 * <p> 익명 클래스를 이용하는 것을 시도해봤으나 익명 클래스는 지원이 안 되는 듯하다.
 * {@link ByteBuddy}를 이용하여 아래 코드처럼 동적으로 다수의 핸들러 타입을 정의하고
 * 인스턴스를 생성하여 등록한다.
 *
 * <pre><code>
 * public class DynamicTypeHandler extends CodeEnumTypeHandler<CodeEnum을_구현한_Enum> {
 *     public DynamicTypeHandler() {
 *         super(CodeEnum을_구현한_Enum.class);
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

    public DynamicCodeEnumTypeHandlerGenerator(Class<?> basePackageClass) {
        this(basePackageClass.getPackage().getName());
    }

    public DynamicCodeEnumTypeHandlerGenerator(String basePackage) {
        this(new Reflections(Objects.requireNonNull(basePackage, "Base package should not be null")));
    }

    public DynamicCodeEnumTypeHandlerGenerator(Reflections reflections) {
        this.reflections = Objects.requireNonNull(reflections, "Reflections should not be null");
    }

    /**
     * Finds all subclasses of {@link CodeEnumTypeHandler} and instantiates them.
     *
     * @return all subclasses of {@link CodeEnumTypeHandler}
     * @throws ReflectiveOperationException if failed to instantiate class
     */
    public Map<Class<?>, TypeHandler<?>> generateAll() throws ReflectiveOperationException {
        return generate0(Policy.INCLUDE);
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
        return generate0(policy, classes);
    }

    private <T extends Enum & CodeEnum> Map<Class<?>, TypeHandler<?>> generate0(Policy policy, Class<T>... classes)
            throws ReflectiveOperationException {
        Set<Class<? extends CodeEnum>> subclasses = this.reflections.getSubTypesOf(CodeEnum.class);

        // "java.lang.Class"를 파라미터로 갖는 "CodeEnumTypeHandler"의 생성자.
        Class<CodeEnumTypeHandler> superType = CodeEnumTypeHandler.class;
        Constructor<CodeEnumTypeHandler> superConstructor = superType.getConstructor(Class.class);

        ClassLoader classLoader = getClass().getClassLoader();

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

            // 'CodeEnumTypeHandler'를 상속하는 동적 타입을 생성한다.
            Class<? extends CodeEnumTypeHandler> dynamicType = new ByteBuddy().subclass(superType)
                    .defineConstructor(Visibility.PUBLIC).intercept(MethodCall.invoke(superConstructor).with(type))
                    .make().load(classLoader).getLoaded();

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
