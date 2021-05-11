package io.github.imsejin.mybatis.pagination.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.imsejin.mybatis.pagination.model.Paginator;

import java.io.IOException;

public class PaginatorSerializer extends StdSerializer<Paginator<?>> {

    /**
     * Prevent {@code org.springframework.web.HttpMediaTypeNotAcceptableException} from occurring.
     */
    public PaginatorSerializer() {
        this(null);
    }

    public PaginatorSerializer(Class<Paginator<?>> type) {
        super(type);
    }

    @Override
    public void serialize(Paginator<?> paginator, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("page", paginator.getPage());
        gen.writeObjectField("items", paginator.getItems());
        gen.writeEndObject();
    }

}
