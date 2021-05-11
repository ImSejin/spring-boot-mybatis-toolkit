package io.github.imsejin.mybatis.pagination.dialect;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;

public interface Dialect {

    String PROPERTY_NAME = "dialect";

    BoundSql createCountBoundSql(BoundSql originalBoundSql, Configuration config);

    BoundSql createOffsetLimitBoundSql(BoundSql originalBoundSql, Configuration config);

}
