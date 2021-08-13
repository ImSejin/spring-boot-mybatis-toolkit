package io.github.imsejin.mybatis.pagination.dialect;

import io.github.imsejin.mybatis.pagination.model.Pageable;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;

public interface Dialect {

    String MAPPED_PARAMETER_CHARACTER = "?";

    /**
     * Creates bound SQL for total items.
     *
     * @param origin original bound SQL
     * @param config configuration
     * @return bound SQL for total items
     */
    BoundSql createCountBoundSql(BoundSql origin, Configuration config);

    /**
     * Creates bound SQL for pagination.
     *
     * @param origin   original bound SQL
     * @param config   configuration
     * @param pageable pageable
     * @return bound SQL for pagination
     */
    BoundSql createOffsetLimitBoundSql(BoundSql origin, Configuration config, Pageable pageable);

}
