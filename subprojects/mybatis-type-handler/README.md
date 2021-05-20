
---

<h3 align="center">MyBatis Type Handler</h3>

---

<p align="center">Type handler generator for integration MyBatis and Spring framework</p>

<p align="center">
    <img alt="Maven Central" src="https://img.shields.io/maven-central/v/io.github.imsejin/mybatis-type-handler">
    <img alt="jdk8" src="https://img.shields.io/badge/jdk-8-orange">
</p>

## Getting started

### Maven

```xml
<dependency>
  <groupId>io.github.imsejin</groupId>
  <artifactId>mybatis-type-handler</artifactId>
  <version>${mybatis.type-handler.version}</version>
</dependency>
```

### Gradle

```groovy
implementation group: "io.github.imsejin", name: "mybatis-type-handler", version: "$mybatisTypeHandlerVersion"
```



## Example

```java
@Configuration
public class AppConfig {

    @Bean
    @Primary
    Reflections reflections() {
        return new Reflections(Application.class); // Base package class
    }
    
    @Bean
    @Primary
    TypeHandlers typeHandlers(Reflections reflections) throws ReflectiveOperationException {
        DynamicCodeEnumTypeHandlerGenerator generator = new DynamicCodeEnumTypeHandlerGenerator(reflections);

        return TypeHandlers.builder()
                .add(generator.generateAll()) // io.github.imsejin.mybatis.typehandler.model.CodeEnum
                .add(UUID::toString, UUID::fromString) // java.util.UUID
                .build();
    }

}
```

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

        // Registers type handlers.
        Map<Class<?>, TypeHandler<?>> typeHandlerMap = typeHandlers.get();
        factoryBean.setTypeHandlers(typeHandlerMap.values().toArray(new TypeHandler[0]));

        return factoryBean.getObject();
    }
    
}
```

```java
@Getter
@ToString
public enum YesOrNo implements CodeEnum {
    YES("Y", "Yes"),
    NO("N", "No");
    
    private final String code;
    private final String codeName;
}
```

```java
@Data
public class Book {
    private UUID uuid;
    private String title;
    private LocalDate publishedAt;
    private YesOrNo outOfPrinted;
}
```

```java
@Mapper
public interface BookMapper {
    
    @Select("SELECT * " +
            "FROM BOOK " +
            "WHERE TITLE LIKE CONCAT('%', #{title}, '%') " +
            "AND PUBLISHED_AT >= #{publishedAt} " +
            "AND OUT_OF_PRINTED = #{outOfPrinted}")
    List<Book> selectBooks(Map<String, Object> query);
    
}
```

```java
@Service
@RequiredArgsConstructor
public class BookService {
    
    private final BookMapper bookMapper;
    
    public List<Book> getBooks() {
        Map<String, Object> query = new HashMap<>();
        query.put("title", "crim");
        query.put("publishedAt", LocalDate.of(1900, 1, 1));
        query.put("outOfPrinted", YesOrNo.YES);
        
        return bookMapper.selectBooks(query);
    }
    
}
```

When invoke `BookService.getBooks()`,

then you will get the following response.

```json
[
    {
        "uuid": "f4b1dc43-a5e9-4d8f-b19b-467cfe4970be",
        "name": "The Brute in the Criminal City",
        "publishedAt": "1913-12-27",
        "outOfPrinted": "Y"
    },
    {
        "uuid": "ef37b3a9-7163-4572-8334-ac31609e5519",
        "name": "Crime of the Three-Inch Inspector",
        "publishedAt": "1908-04-11",
        "outOfPrinted": "Y"
    }
]
```
