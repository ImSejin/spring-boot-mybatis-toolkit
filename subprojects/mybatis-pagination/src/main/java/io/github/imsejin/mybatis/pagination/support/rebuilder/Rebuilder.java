package io.github.imsejin.mybatis.pagination.support.rebuilder;

import io.github.imsejin.mybatis.pagination.constant.RebuildMode;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;

public interface Rebuilder<T> {

    static MappedStatementRebuilder init(MappedStatement ms, RebuildMode rebuildMode) {
        return new MappedStatementRebuilder(ms, rebuildMode);
    }

    static SqlSourceRebuilder init(Configuration config) {
        return new SqlSourceRebuilder(config);
    }

    static BoundSqlRebuilder init(BoundSql boundSql, RebuildMode rebuildMode) {
        return new BoundSqlRebuilder(boundSql, rebuildMode);
    }

    T rebuild();

}
