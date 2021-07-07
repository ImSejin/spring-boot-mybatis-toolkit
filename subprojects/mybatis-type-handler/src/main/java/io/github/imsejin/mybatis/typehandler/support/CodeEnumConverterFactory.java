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

package io.github.imsejin.mybatis.typehandler.support;

import io.github.imsejin.mybatis.typehandler.model.CodeEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.ConverterRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @see ConverterRegistry#addConverterFactory(ConverterFactory)
 */
public class CodeEnumConverterFactory<E extends Enum<E> & CodeEnum> implements ConverterFactory<String, E> {

    @Override
    @SuppressWarnings("unchecked")
    public <T extends E> Converter<String, T> getConverter(Class<T> targetType) {
        return (Converter<String, T>) new StringToEnumConverter((Class<E>) targetType);
    }

    private class StringToEnumConverter implements Converter<String, E> {
        private final Map<String, E> cache;

        private StringToEnumConverter(Class<E> enumType) {
            E[] enumConstants = enumType.getEnumConstants();

            // Creates a cache for fast lookup.
            // DON'T DO THIS USING STREAM AND LAMBDA EXPRESSION BECAUSE OF LambdaConversionException.
            Map<String, E> cache = new HashMap<>();
            for (E codeEnum : enumConstants) {
                cache.put(codeEnum.getCode(), codeEnum);
            }

            this.cache = Collections.unmodifiableMap(cache);
        }

        @Override
        public E convert(String source) {
            if (!this.cache.containsKey(source)) return null;
            return this.cache.get(source);
        }
    }

}
