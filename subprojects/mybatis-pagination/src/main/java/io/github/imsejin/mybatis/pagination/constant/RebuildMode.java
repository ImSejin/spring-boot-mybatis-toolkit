package io.github.imsejin.mybatis.pagination.constant;

import io.github.imsejin.mybatis.pagination.support.rebuilder.Rebuilder;
import org.apache.ibatis.executor.BaseExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.BeanWrapper;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Map;

/**
 * Mode for rebuilder.
 *
 * @see Rebuilder#rebuild()
 */
public enum RebuildMode {

    /**
     * Mode for copy parameter of mapper method as own type.
     */
    COPY,

    /**
     * Mode for copy parameter of mapper method as {@link Map}.
     *
     * <p> When query has additional parameter(e.g. {@code <foreach />, <bind />, ...}) and
     * the parameter type of mapper method is {@link MapperParameterType#SINGLE},
     * {@link ReflectionException} will be occurred and print the message like this.
     *
     * <pre>
     *     There is no getter for property named '__frch_parameterName_0' in
     *     'class io.github.imsejin.mybatis.pagination.model.PageRequest'
     * </pre>
     *
     * <p> The cause of this problem is a internal reflector of MyBatis tries to
     * find dynamic property at the static bean parameter.
     *
     * <ol>
     *     <li>{@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler)}</li>
     *     <li>{@link BaseExecutor#createCacheKey(MappedStatement, Object, RowBounds, BoundSql)}</li>
     *     <li>{@link Configuration#newMetaObject(Object)}</li>
     *     <li>{@link MetaObject#forObject(Object, ObjectFactory, ObjectWrapperFactory, ReflectorFactory)}</li>
     *     <li>{@link BeanWrapper#BeanWrapper(MetaObject, Object)}</li>
     * </ol>
     *
     * <p> {@link BeanWrapper} is for static java bean(POJO). This wrapper can't
     * handle dynamic properties. <u>The solution is wrapping original parameter object
     * with {@link Map}.</u> Using the solution, {@link MetaObject} will be instantiated
     * with {@link MapWrapper} instead of {@link BeanWrapper}.
     *
     * @see DynamicSqlSource
     * @see SqlNode
     * @see BeanWrapper#get(PropertyTokenizer)
     */
    WRAP;

}
