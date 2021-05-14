package io.github.imsejin.mybatis.pagination.dialect;

import io.github.imsejin.mybatis.pagination.model.Pageable;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;

public interface Dialect {

    String PROPERTY_NAME = "dialect";

    BoundSql createCountBoundSql(BoundSql origin, Configuration config);

    BoundSql createOffsetLimitBoundSql(BoundSql origin, Configuration config, Pageable pageable);

}
