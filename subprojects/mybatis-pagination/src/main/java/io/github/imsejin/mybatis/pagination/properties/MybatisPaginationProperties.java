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

package io.github.imsejin.mybatis.pagination.properties;

import io.github.imsejin.mybatis.pagination.dialect.Dialect;
import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

/**
 * Properties for plugin of MyBatis pagination.
 */
@Getter
@ToString
@ConfigurationProperties(MybatisPaginationProperties.PREFIX)
public class MybatisPaginationProperties {

    public static final String PREFIX = "mybatis.configuration.plugins.pagination";

    private final Properties properties = new Properties();

    private final AutoConfigure autoConfigure = new AutoConfigure();

    private Class<? extends Dialect> dialectClass;

    public void setDialectClass(Class<? extends Dialect> dialectClass) {
        this.dialectClass = dialectClass;
        properties.setProperty("dialectClass", dialectClass.toString());
    }

    @Getter
    @ToString
    public class AutoConfigure {
        private boolean enabled;

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
            properties.setProperty("autoConfigure.enabled", String.valueOf(enabled));
        }
    }

}
