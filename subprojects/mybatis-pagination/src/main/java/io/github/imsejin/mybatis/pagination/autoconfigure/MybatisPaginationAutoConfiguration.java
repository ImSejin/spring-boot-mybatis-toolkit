/*
 * MIT License
 *
 * Copyright (c) 2021 Im Sejin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.imsejin.mybatis.pagination.autoconfigure;

import io.github.imsejin.mybatis.pagination.dialect.Dialect;
import io.github.imsejin.mybatis.pagination.interceptor.PaginationInterceptor;
import io.github.imsejin.mybatis.pagination.properties.MybatisPaginationProperties;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.List;

/**
 * Auto-configuration for plugin of MyBatis pagination.
 */
@org.springframework.context.annotation.Configuration
@ConditionalOnBean(SqlSessionFactory.class)
@AutoConfigureAfter(MybatisAutoConfiguration.class)
@EnableConfigurationProperties(MybatisPaginationProperties.class)
@RequiredArgsConstructor
public class MybatisPaginationAutoConfiguration implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(MybatisPaginationAutoConfiguration.class);

    private static final Class<PaginationInterceptor> INTERCEPTOR_CLASS = PaginationInterceptor.class;

    private final List<SqlSessionFactory> sqlSessionFactories;

    private final MybatisPaginationProperties properties;

    private static boolean hasInterceptor(Configuration configuration) {
        for (Interceptor interceptor : configuration.getInterceptors()) {
            if (INTERCEPTOR_CLASS.isAssignableFrom(interceptor.getClass())) return true;
        }

        return false;
    }

    @Override
    public void afterPropertiesSet() {
        if (!this.properties.getAutoConfigure().isEnabled()) return;

        Dialect dialect;
        try {
            dialect = this.properties.getDialectClass().getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(String.format("Cannot find a public default constructor of '%s'",
                    this.properties.getDialectClass()), e);
        }

        Interceptor interceptor = new PaginationInterceptor(dialect);
        interceptor.setProperties(this.properties.getProperties());

        for (SqlSessionFactory sqlSessionFactory : this.sqlSessionFactories) {
            Configuration configuration = sqlSessionFactory.getConfiguration();
            if (hasInterceptor(configuration)) continue;

            configuration.addInterceptor(interceptor);
            logger.debug("Plugin '{}' is registered with '{}'", INTERCEPTOR_CLASS.getSimpleName(), sqlSessionFactory);
        }
    }

}
