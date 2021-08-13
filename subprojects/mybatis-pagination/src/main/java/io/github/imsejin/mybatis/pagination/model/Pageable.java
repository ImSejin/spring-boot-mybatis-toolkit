package io.github.imsejin.mybatis.pagination.model;

public interface Pageable {

    /**
     * Returns offset.
     *
     * @return offset
     */
    int getOffset();

    /**
     * Returns limit.
     *
     * @return limit
     */
    int getLimit();

}
