package io.github.imsejin.mybatis.pagination.support.rebuilder;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.util.List;

public class SqlSourceRebuilder implements Rebuilder<SqlSource> {

    private final Configuration config;
    private String sql;
    private List<ParameterMapping> parameterMappings;

    SqlSourceRebuilder(Configuration config) {
        this.config = config;
    }

    public SqlSourceRebuilder sql(String sql) {
        this.sql = sql;
        return this;
    }

    public SqlSourceRebuilder parameterMappings(List<ParameterMapping> parameterMappings) {
        this.parameterMappings = parameterMappings;
        return this;
    }

    public SqlSourceRebuilder boundSql(BoundSql boundSql) {
        this.sql = boundSql.getSql();
        this.parameterMappings = boundSql.getParameterMappings();
        return this;
    }

    @Override
    public SqlSource rebuild() {
        return new StaticSqlSource(this.config, this.sql, this.parameterMappings);
    }

}
