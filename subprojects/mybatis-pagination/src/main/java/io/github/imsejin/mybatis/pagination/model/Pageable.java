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

    /**
     * Returns start row number.
     *
     * @return start row number
     */
    int getStartRowNum();

    /**
     * Returns end row number.
     *
     * @return end row number
     */
    int getEndRowNum();

}
