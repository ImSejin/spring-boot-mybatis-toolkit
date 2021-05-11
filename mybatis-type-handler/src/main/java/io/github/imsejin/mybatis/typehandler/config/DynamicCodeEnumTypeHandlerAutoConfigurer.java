package io.github.imsejin.mybatis.typehandler.config;

import io.github.imsejin.mybatis.typehandler.CodeEnumTypeHandler;
import io.github.imsejin.mybatis.typehandler.model.CodeEnum;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.implementation.MethodCall;
import org.apache.ibatis.type.TypeHandler;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
@SuppressWarnings("rawtypes")
public class DynamicCodeEnumTypeHandlerAutoConfigurer {

    private final Reflections reflections;

    public DynamicCodeEnumTypeHandlerAutoConfigurer(Class<?> basePackageClass) {
        this(basePackageClass.getPackage().getName());
    }

    public DynamicCodeEnumTypeHandlerAutoConfigurer(String basePackage) {
        this(new Reflections(basePackage));
    }

    public DynamicCodeEnumTypeHandlerAutoConfigurer(Reflections reflections) {
        this.reflections = reflections;
    }

    public Map<Class<? extends CodeEnum>, TypeHandler<?>> findTypeHandlers() throws ReflectiveOperationException {
        Set<Class<? extends CodeEnum>> subclasses = this.reflections.getSubTypesOf(CodeEnum.class);

        // 'java.lang.Class'를 파라미터로 갖는 'CodeEnumTypeHandler'의 생성자.
        Class<CodeEnumTypeHandler> superType = CodeEnumTypeHandler.class;
        Constructor<CodeEnumTypeHandler> superConstructor = superType.getConstructor(Class.class);

        ClassLoader classLoader = getClass().getClassLoader();

        Map<Class<? extends CodeEnum>, TypeHandler<?>> typeHandlerMap = new HashMap<>();
        for (Class<? extends CodeEnum> type : subclasses) {
            /*
             * 'Enum'이 아닌 타입이거나, abstract method를 갖고 있는 'Enum'의 경우
             * 각 constant가 anonymous class로 생성된다.
             * 이런 타입으로 'CodeEnumTypeHandler'를 생성할 수 없다.
             */
            if (!Enum.class.isAssignableFrom(type) || type.isAnonymousClass()) continue;

            // 'CodeEnumTypeHandler'를 상속하는 동적 타입을 생성한다.
            Class<? extends CodeEnumTypeHandler> dynamicType = new ByteBuddy().subclass(superType)
                    .defineConstructor(Visibility.PUBLIC).intercept(MethodCall.invoke(superConstructor).with(type))
                    .make().load(classLoader).getLoaded();

            // 기본 생성자로 동적 타입의 인스턴스를 생성한다.
            Constructor<? extends CodeEnumTypeHandler> constructor = dynamicType.getConstructor();
            TypeHandler<?> dynamicTypeHandler = constructor.newInstance();

            typeHandlerMap.put(type, dynamicTypeHandler);
        }

        return typeHandlerMap;
    }

}
