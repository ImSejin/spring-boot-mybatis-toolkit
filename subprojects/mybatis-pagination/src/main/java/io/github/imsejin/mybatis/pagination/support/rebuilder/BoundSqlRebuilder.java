package io.github.imsejin.mybatis.pagination.support.rebuilder;

import io.github.imsejin.mybatis.pagination.constant.MapperParameterType;
import io.github.imsejin.mybatis.pagination.constant.RebuildMode;
import io.github.imsejin.mybatis.pagination.model.PageRequest;
import io.github.imsejin.mybatis.pagination.model.Pageable;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @see RebuildMode#WRAP
 */
public class BoundSqlRebuilder implements Rebuilder<BoundSql> {

    private final BoundSql boundSql;
    private final RebuildMode rebuildMode;
    private final MapperParameterType mapperParameterType;
    private Object parameterObject;
    private Configuration config;
    private String sql;
    private List<ParameterMapping> parameterMappings;

    BoundSqlRebuilder(BoundSql boundSql, RebuildMode rebuildMode) {
        this.boundSql = boundSql;
        this.rebuildMode = rebuildMode;
        this.mapperParameterType = MapperParameterType.from(boundSql);
        this.parameterObject = boundSql.getParameterObject();
    }

    public BoundSqlRebuilder config(Configuration config) {
        this.config = config;
        return this;
    }

    public BoundSqlRebuilder sql(String sql) {
        this.sql = sql;
        return this;
    }

    public BoundSqlRebuilder parameterMappings(List<ParameterMapping> parameterMappings) {
        this.parameterMappings = parameterMappings;
        return this;
    }

    @Override
    public BoundSql rebuild() {
        // Sets options from original bound SQL.
        if (this.parameterMappings == null) this.parameterMappings = this.boundSql.getParameterMappings();

        // Converts the parameter type of mapper method to "java.util.Map".
        if (this.rebuildMode == RebuildMode.WRAP && this.mapperParameterType == MapperParameterType.SINGLE) {
            this.parameterObject = wrap();
        }

        return new BoundSql(this.config, this.sql, this.parameterMappings, this.parameterObject);
    }

    @SuppressWarnings("unchecked")
    private Object wrap() {
        Object parameterObject = this.parameterObject;

        // Checks if additional parameters exist.
        Map<String, Object> additionalParameterMap = getAdditionalParameters();

        // Finds additional parameters and merges them with static parameters as total parameters.
        if (!additionalParameterMap.isEmpty()) {
            switch (this.mapperParameterType) {
                case NONE:
                    return null;

                case SINGLE:
                    // Merges the additional parameters with query of PageRequest.
                    Pageable pageable = (Pageable) parameterObject;

                    if (pageable instanceof PageRequest) {
                        PageRequest pageRequest = (PageRequest) pageable;
                        Map<String, Object> param = new HashMap<>(pageRequest.getQuery());
                        param.putAll(additionalParameterMap);

                        param.forEach(this.boundSql::setAdditionalParameter);

                        // Substitutes a merged map for parameter object.
                        return param;
                    }
                    break;

                case MULTIPLE:
                    // Merges the additional parameters with a instance of ParamMap.
                    Map<String, Object> param = (MapperMethod.ParamMap<Object>) parameterObject;
                    param.putAll(additionalParameterMap);

                    // Substitutes a merged map for parameter object.
                    return param;

                default:
                    throw new TypeException("Failed to find the number of parameters: " + parameterObject);
            }
        }

        return parameterObject;
    }

    private Map<String, Object> getAdditionalParameters() {
        Map<String, Object> additionalParameters = new HashMap<>();

        for (ParameterMapping parameterMapping : this.boundSql.getParameterMappings()) {
            String property = parameterMapping.getProperty();
            if (!this.boundSql.hasAdditionalParameter(property)) continue;

            additionalParameters.put(property, this.boundSql.getAdditionalParameter(property));
        }

        return additionalParameters;
    }

}

