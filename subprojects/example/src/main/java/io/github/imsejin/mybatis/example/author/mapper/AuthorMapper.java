package io.github.imsejin.mybatis.example.author.mapper;

import io.github.imsejin.mybatis.example.author.model.Author;
import io.github.imsejin.mybatis.pagination.model.Pageable;
import io.github.imsejin.mybatis.pagination.model.Paginator;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AuthorMapper {

    @ResultMap("author")
    @Select("<script> " +
            "SELECT ID, NAME, COUNTRY, BIRTHDATE, UUID() AS UUID " +
            "FROM AUTHOR " +
            "WHERE 1 " +

            "<if test='query.name != null'> " +
            "   AND NAME LIKE CONCAT('%', TRIM(#{query.name}), '%') " +
            "</if> " +

            "<if test='query.country != null'> " +
            "   AND COUNTRY = #{query.country} " +
            "</if> " +

            "<if test='query.startDate != null'> " +
            "   AND BIRTHDATE &gt; #{query.startDate} " +
            "</if> " +

            "<if test='query.endDate != null'> " +
            "   AND BIRTHDATE &lt; #{query.endDate} " +
            "</if> " +

            "<if test='query.number != null'> " +
            "   <bind name='items' value='query.number.split(\",\")' /> " +
            "   OR ID IN " +
            "   <foreach item='item' collection='items' open='(' separator=',' close=')'> " +
            "       #{item} " +
            "   </foreach> " +
            "</if> " +

            "</script>")
    Paginator<Author> selectAll(Pageable pageable);

    @Results(id = "author", value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "name", column = "NAME"),
            @Result(property = "country", column = "COUNTRY"),
            @Result(property = "birthdate", column = "BIRTHDATE"),
            @Result(property = "uuid", column = "UUID"),
    })
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
