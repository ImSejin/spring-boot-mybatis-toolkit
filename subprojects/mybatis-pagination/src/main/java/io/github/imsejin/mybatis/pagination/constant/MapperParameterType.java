package io.github.imsejin.mybatis.pagination.constant;

import io.github.imsejin.mybatis.pagination.support.InterceptorSupport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

public enum MapperParameterType {

    /**
     * Mapper method has no parameters.
     */
    NONE,

    /**
     * Mapper method has one parameter
     * that is not annotated with {@link Param}.
     */
    SINGLE,

    /**
     * Mapper method has multiple parameters or
     * one parameter that is annotated with {@link Param}.
     */
    MULTIPLE;

    public static MapperParameterType from(BoundSql boundSql) {
        Object paramObject = boundSql.getParameterObject();

        if (paramObject == null) {
            return NONE;
        } else if (paramObject instanceof MapperMethod.ParamMap) {
            return MULTIPLE;
        } else {
            return SINGLE;
        }
    }

    public static MapperParameterType from(MappedStatement ms) {
        Method mapperMethod = InterceptorSupport.findMethod(ms);

        if (Arrays.stream(mapperMethod.getParameterAnnotations()).flatMap(Arrays::stream)
                .anyMatch(it -> it.annotationType() == Param.class)) return MULTIPLE;

        Parameter[] parameters = mapperMethod.getParameters();
        if (parameters.length > 1) {
            return MULTIPLE;
        } else if (parameters.length == 1) {
            return SINGLE;
        } else {
            return NONE;
        }
    }

}
