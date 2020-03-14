package io.snice.networking.diameter.yaml;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.function.Function;

public class StandardAvpDeserializer<T> extends JsonDeserializer<T> {

    private final Function<String, T> creator;

    public StandardAvpDeserializer(final Function<String, T> creator) {
        this.creator = creator;
    }

    @Override
    public T deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return creator.apply(p.getValueAsString());
    }
}
