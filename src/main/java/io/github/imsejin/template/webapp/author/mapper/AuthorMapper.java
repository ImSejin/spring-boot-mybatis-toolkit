package io.github.imsejin.template.webapp.author.mapper;

import io.github.imsejin.template.webapp.author.model.Author;
import io.github.imsejin.template.webapp.core.database.mybatis.model.Page;
import io.github.imsejin.template.webapp.core.database.mybatis.model.Paginator;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

@Mapper
public interface AuthorMapper {

    @Select("<script>" +
            "SELECT *, #{number} AS NUMBER " +
            "FROM AUTHOR " +
            "<if test='size lt 10'> " +
            "WHERE ID &lt; #{size} " +
            "</if> " +
            "<if test='size gte 10'> " +
            "WHERE ID >= #{size} " +
            "</if> " +
             "LIMIT #{limit} OFFSET #{offset} " +
            "</script>")
    Paginator<Author> selectAll(Page page);

    @Select("SELECT *, /* ? asd ? */#{id} AS ID -- asdas ? \n" +
            "FROM AUTHOR " +
            "WHERE ID = #{id} " +
            "OR ID = #{param2.number}")
    Paginator<Author> selectById(@Param("id") long id, Page page);
//    Author selectById(@Param("id") long id, int i);

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
