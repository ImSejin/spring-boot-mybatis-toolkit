package io.github.imsejin.template.webapp.core.database;

import com.zaxxer.hikari.HikariDataSource;
import io.github.imsejin.template.webapp.Application;
import io.github.imsejin.template.webapp.core.database.mybatis.config.DynamicCodeEnumTypeHandlerAutoConfig;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.h2.tools.Server;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Cannot refer to both {@link SqlSessionTemplate} and {@link SqlSessionFactory} together.
 * If you do, {@link SqlSessionFactory} is ignored.
 */
@org.springframework.context.annotation.Configuration
@MapperScan(
        annotationClass = Mapper.class,
        basePackageClasses = Application.class,
        sqlSessionFactoryRef = "sqlSessionFactory"
)
@RequiredArgsConstructor
public class DatabaseConfig {

    private final Server server;

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
        factoryBean.setTypeHandlers(codeEnumTypeHandlers.get().toArray(new TypeHandler[0]));

        return factoryBean.getObject();
    }

}
