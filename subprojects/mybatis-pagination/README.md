
---

<h3 align="center">MyBatis Pagination</h3>

---

<p align="center">Pagination feature for integration MyBatis and Spring framework</p>

<p align="center">
    <a href="https://lgtm.com/projects/g/ImSejin/spring-boot-mybatis-toolkit/context:java"><img alt="Language grade: Java" src="https://img.shields.io/lgtm/grade/java/g/ImSejin/spring-boot-mybatis-toolkit.svg?logo=lgtm&logoWidth=18"/></a>
    <a href="https://search.maven.org/artifact/io.github.imsejin/mybatis-pagination">
        <img alt="Maven Central" src="https://img.shields.io/maven-central/v/io.github.imsejin/mybatis-pagination">
    </a>
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
@Component
public class PageRequestResolver extends PageRequestResolverAdaptor {

    public PageRequestResolver(ObjectMapper objectMapper) {
        super(objectMapper);
    }

}

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final Reflections reflections;
    private final ApplicationContext context;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        Set<Class<? extends HandlerMethodArgumentResolver>> resolverTypes = reflections
                .getSubTypesOf(HandlerMethodArgumentResolver.class).stream()
                .filter(it -> !Modifier.isAbstract(it.getModifiers())).collect(toSet()); // Excludes adaptor classes.

        resolverTypes.stream().map(context::getBean).forEach(resolvers::add);
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

