package io.github.imsejin.template.webapp.author.mapper;

import io.github.imsejin.template.webapp.author.model.Author;
import io.github.imsejin.template.webapp.core.database.mybatis.model.pagination.Pageable;
import io.github.imsejin.template.webapp.core.database.mybatis.model.pagination.Paginator;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AuthorMapper {

    @Select("<script> " +
            "SELECT * " +
            "FROM AUTHOR " +
            "WHERE 1 " +

            "<if test='query.name != null'> " +
            "AND NAME LIKE CONCAT('%', #{query.name}, '%') " +
            "</if> " +

            "<if test='query.country != null'> " +
            "AND COUNTRY = #{query.country} " +
            "</if> " +

            "<if test='query.startDate != null'> " +
            "AND BIRTHDATE &lt; #{query.startDate} " +
            "</if> " +

            "<if test='query.endDate != null'> " +
            "AND BIRTHDATE &lt; #{query.endDate} " +
            "</if> " +

             "LIMIT #{limit} OFFSET #{offset} " +
            "</script>")
    Paginator<Author> selectAll(Pageable pageable);

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
