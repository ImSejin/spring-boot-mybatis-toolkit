package io.github.imsejin.template.webapp.core.database.mybatis.dialect;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;

public interface Dialect {

    String PROPERTY_NAME = "dialect";

    BoundSql createCountBoundSql(BoundSql originalBoundSql, Configuration config);

    BoundSql createOffsetLimitBoundSql(BoundSql originalBoundSql, Configuration config);

}
