
---

<h3 align="center">MyBatis Pagination</h3>

---

<p align="center">Pagination feature for integration MyBatis and Spring framework</p>

<p align="center">
    <a href="https://lgtm.com/projects/g/ImSejin/spring-boot-mybatis-toolkit/context:java">
        <img alt="Lgtm grade" src="https://img.shields.io/lgtm/grade/java/g/ImSejin/spring-boot-mybatis-toolkit.svg?logo=&logoWidth=&label=lgtm%3A%20code%20quality&&style=flat-square"/>
    </a>
    <a href="https://www.codacy.com/gh/ImSejin/spring-boot-mybatis-toolkit/dashboard">
        <img alt="Codacy grade" src="https://img.shields.io/codacy/grade/6bf43df6f96d4b63892d4acf13c4e0a4?label=codacy%3A%20code%20quality&style=flat-square">
    </a>
    <a href="https://search.maven.org/artifact/io.github.imsejin/mybatis-pagination">
        <img alt="Maven Central" src="https://img.shields.io/maven-central/v/io.github.imsejin/mybatis-pagination?style=flat-square">
    </a>
    <img alt="jdk8" src="https://img.shields.io/badge/jdk-8-orange?style=flat-square">
</p>

## Getting started

### Maven

```xml
<dependency>
  <groupId>io.github.imsejin</groupId>
  <artifactId>mybatis-pagination</artifactId>
  <version>${mybatis-pagination.version}</version>
</dependency>
```

### Gradle

```groovy
implementation group: "io.github.imsejin", name: "mybatis-pagination", version: "$mybatisPaginationVersion"
```



## Changelog

[here](./CHANGELOG.md)



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
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApplicationContext context;

    @Bean
    @Primary
    PageRequestResolver pageRequestResolver(ObjectMapper objectMapper) {
        return new PageRequestResolver(objectMapper);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.addAll(context.getBeansOfType(HandlerMethodArgumentResolver.class).values());
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
            "AND BIRTHDATE = #{query.birthdate}")
    Paginator<Author> selectAuthors(Pageable pageable);
    
}
```

When request `GET /authors?page=1&size=2&query={"name":"nu","birthdate":"1801-09-19"}`,

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

