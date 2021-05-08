package io.github.imsejin.template.webapp.core.database.mybatis.mapper;

import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseMapper<T> {

    List<T> selectAll();

    T selectById(long id);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(T item);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertAll(@Param("item") T... items);

    void update(T item);

    void delete(T item);

}
