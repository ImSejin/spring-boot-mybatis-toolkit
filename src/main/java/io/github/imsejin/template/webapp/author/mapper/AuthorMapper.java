package io.github.imsejin.template.webapp.author.mapper;

import io.github.imsejin.template.webapp.author.model.Author;
import io.github.imsejin.template.webapp.core.database.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AuthorMapper extends BaseMapper<Author> {

    @Select("SELECT * " +
            "FROM AUTHOR")
    List<Author> selectAll();

    @Select("SELECT * " +
            "FROM AUTHOR " +
            "WHERE ID = #{id}")
    Author selectById(long id);

    @Select("SELECT * " +
            "FROM AUTHOR " +
            "WHERE NAME = #{name} " +
            "LIMIT 1")
    Author selectByName(String name);

    @Insert("INSERT INTO AUTHOR (NAME, COUNTRY, BIRTHDATE) VALUES " +
            "(#{name}, #{country}, #{birthdate})")
    void insert(Author author);

    @Insert("<script> " +
            "INSERT INTO AUTHOR (NAME, COUNTRY, BIRTHDATE) VALUES " +
            "<foreach item='author' collection='authors' separator=','> " +
            "(#{author.name}, #{author.country}, #{author.birthdate}) " +
            "</foreach> " +
            "</script>")
    void insertAll(@Param("authors") Author... authors);

    @Update("UPDATE AUTHOR " +
            "SET NAME = #{name} " +
            ", COUNTRY = #{country} " +
            ", BIRTHDATE = #{birthdate} " +
            "WHERE ID = #{id}")
    void update(Author author);

    @Delete("DELETE FROM AUTHOR " +
            "WHERE ID = #{id}")
    void delete(Author author);

}
