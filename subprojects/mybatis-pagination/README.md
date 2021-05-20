
---

<h3 align="center">MyBatis Pagination</h3>

---

<p align="center">Pagination feature for integration MyBatis and Spring framework</p>

<p align="center">
    <img alt="Maven Central" src="https://img.shields.io/maven-central/v/io.github.imsejin/mybatis-pagination">
    <img alt="jdk8" src="https://img.shields.io/badge/jdk-8-orange">
</p>

## Getting started

### Maven

```xml
<dependency>
  <groupId>io.github.imsejin</groupId>
  <artifactId>mybatis-pagination</artifactId>
  <version>${mybatis.pagination.version}</version>
</dependency>
```

### Gradle

```groovy
implementation group: "io.github.imsejin", name: "mybatis-pagination", version: "$mybatisPaginationVersion"
```



## Example

```java
@Configuration
@MapperScan(
        annotationClass = Mapper.class,
        basePackageClasses = Application.class,
        sqlSessionFactoryRef = "sqlSessionFactory"
)
public class DatabaseConfig {
    
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    DataSource dataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }
    
    @Primary
    @Bean("sqlSessionFactory")
    SqlSessionFactory sqlSessionFactory(DataSource dataSource, TypeHandlers typeHandlers) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);

        // Registers interceptors.
        Dialect dialect = new MySQLDialect();
        factoryBean.setPlugins(new PaginationInterceptor(dialect));

        return factoryBean.getObject();
    }
    
}
```

```java
@RestContorller
@RequestMapping("/authors")
@RequiredArgsConstructor
public class AuthorController {
    
    private final AuthorMapper authorMapper;
    
    @GetMapping
    public Object authors(PageRequest pageRequest) {
        return authorMapper.selectAuthors(pageRequest);
    }
    
}
```

```java
@Data
public class Author {
    private long id;
    private String name;
    private String country;
    private LocalDate birthdate;
}
```

```java
@Mapper
public interface AuthorMapper {
    
    @Select("SELECT * " +
            "FROM AUTHOR " +
            "WHERE NAME LIKE CONCAT('%', #{query.name}, '%') " +
            "AND BIRTHDATE = #{query.createdAt}")
    Paginator<Author> selectAuthors(Pageable pageable);
    
}
```

When request `GET /samples?page=1&size=2&query={"name":"nu","createdAt":"1801-09-19"}`,

then you will get the following response.

```json
{
    "pageInfo": {
        "totalItems": 15,
        "page": 1,
        "size": 2,
        "totalPages": 8
    },
    "items": [
        {
            "id": 11,
            "name": "Reihleim Mannum",
            "country": "Botswana",
            "birthdate": "1801-09-19"
        },
        {
            "id": 32,
            "name": "Nai Nun",
            "country": "Cyprus",
            "birthdate": "1801-09-19"
        }
    ]
}
```
