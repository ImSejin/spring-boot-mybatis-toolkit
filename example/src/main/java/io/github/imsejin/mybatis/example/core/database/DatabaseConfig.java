package io.github.imsejin.mybatis.example.core.database;

import com.zaxxer.hikari.HikariDataSource;
import io.github.imsejin.mybatis.example.Application;
import io.github.imsejin.mybatis.example.core.database.mybatis.config.DynamicCodeEnumTypeHandlerAutoConfig;
import io.github.imsejin.mybatis.typehandler.support.TypeHandlerSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

/**
 * Cannot refer to both {@link SqlSessionTemplate} and {@link SqlSessionFactory} together.
 * If you do, {@link SqlSessionFactory} is ignored.
 */
@Slf4j
@org.springframework.context.annotation.Configuration
@MapperScan(
        annotationClass = Mapper.class,
        basePackageClasses = Application.class,
        sqlSessionFactoryRef = "sqlSessionFactory"
)
@RequiredArgsConstructor
public class DatabaseConfig {

    private final DynamicCodeEnumTypeHandlerAutoConfig.CodeEnumTypeHandlers codeEnumTypeHandlers;

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    DataSource dataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Primary
    @Bean("sqlSessionFactory")
    SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();

        /*
        Supports column names with underscore in a result set
        to be converted with camelcase.
         */
        Configuration configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);

        factoryBean.setDataSource(dataSource);
        factoryBean.setConfiguration(configuration);

        Map<Class<?>, TypeHandler<?>> typeHandlerMap = newTypeHandlerMap(codeEnumTypeHandlers);
        factoryBean.setTypeHandlers(typeHandlerMap.values().toArray(new TypeHandler<?>[0]));

        log.info("SqlSessionFactoryBean registered {} type handler(s): {}",
                typeHandlerMap.size(), typeHandlerMap.keySet().stream().map(Class::getSimpleName).collect(toList()));

//        Interceptor interceptor = new PaginatorInterceptor(new MySQLDialect());
//        factoryBean.setPlugins(interceptor);

        return factoryBean.getObject();
    }

    private Map<Class<?>, TypeHandler<?>> newTypeHandlerMap(
            DynamicCodeEnumTypeHandlerAutoConfig.CodeEnumTypeHandlers codeEnumTypeHandlers)
            throws ReflectiveOperationException {
        Map<Class<?>, TypeHandler<?>> typeHandlerMap = new HashMap<>(codeEnumTypeHandlers.get());

        // java.util.UUID
        BaseTypeHandler<UUID> uuidTypeHandler = TypeHandlerSupport.make(UUID.class, UUID::toString, UUID::fromString);
        typeHandlerMap.put(UUID.class, uuidTypeHandler);

        return typeHandlerMap;
    }

}
