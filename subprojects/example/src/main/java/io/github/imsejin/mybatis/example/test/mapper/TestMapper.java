package io.github.imsejin.mybatis.example.test.mapper;

import io.github.imsejin.mybatis.example.core.codeenum.Status;
import io.github.imsejin.mybatis.example.core.codeenum.YesOrNo;
import io.github.imsejin.mybatis.example.core.model.KanCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.UUID;

@Mapper
public interface TestMapper {

    @Select("SELECT #{code}")
    YesOrNo selectYesOrNoByCode(String code);

    @Select("SELECT #{yesOrNo}")
    String selectCodeByYesOrNo(YesOrNo yesOrNo);

    @Select("SELECT #{code}")
    Status selectStatusByCode(String code);

    @Select("SELECT #{status}")
    String selectCodeByStatus(Status status);

    @Select("SELECT #{code}")
    KanCode selectKanCodeByCode(String code);

    @Select("SELECT #{kanCode}")
    String selectCodeByKanCode(KanCode kanCode);

    @Select("SELECT #{uuid}")
    UUID selectUUIDByString(String uuid);

    @Select("SELECT #{uuid}")
    String selectStringByUUID(UUID uuid);

}
