package io.github.imsejin.mybatis.example.core.database;

import com.zaxxer.hikari.HikariDataSource;
import io.github.imsejin.mybatis.example.Application;
import io.github.imsejin.mybatis.pagination.dialect.MySQLDialect;
import io.github.imsejin.mybatis.pagination.interceptor.PaginationInterceptor;
import io.github.imsejin.mybatis.typehandler.support.TypeHandlers;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
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
public class DatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    DataSource dataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Primary
    @Bean("sqlSessionFactory")
    SqlSessionFactory sqlSessionFactory(DataSource dataSource, TypeHandlers.TypeHandlerBuilder builder) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();

        /*
        Supports column names with underscore in a result set
        to be converted with camelcase.
         */
        Configuration configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);

        factoryBean.setDataSource(dataSource);
        factoryBean.setConfiguration(configuration);

        // Registers type handlers.
        Map<Class<?>, TypeHandler<?>> typeHandlerMap = newTypeHandlerMap(builder);
        factoryBean.setTypeHandlers(typeHandlerMap.values().toArray(new TypeHandler[0]));
        log.info("SqlSessionFactoryBean registered {} type handler(s): {}",
                typeHandlerMap.size(), typeHandlerMap.keySet().stream().map(Class::getSimpleName).collect(toList()));

        // Registers interceptors.
        List<Interceptor> interceptors = Arrays.asList(new PaginationInterceptor(new MySQLDialect()));
        factoryBean.setPlugins(interceptors.toArray(new Interceptor[0]));
        log.info("SqlSessionFactoryBean registered {} interceptor(s): {}", interceptors.size(),
                interceptors.stream().map(it -> it.getClass().getSimpleName()).collect(toList()));

        return factoryBean.getObject();
    }

    private Map<Class<?>, TypeHandler<?>> newTypeHandlerMap(TypeHandlers.TypeHandlerBuilder builder) {
        // java.util.UUID
        builder.add(UUID.class, UUID::toString, UUID::fromString);

        return builder.build().get();
    }

}
