package io.snice.networking.diameter.yaml;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.ReferenceType;

public class DiameterAvpDeserializers extends Deserializers.Base {
    @Override
    public JsonDeserializer<?> findReferenceDeserializer(final ReferenceType refType,
                                                         final DeserializationConfig config,
                                                         final BeanDescription beanDesc,
                                                         final TypeDeserializer contentTypeDeserializer,
                                                         final JsonDeserializer<?> contentDeserializer) throws JsonMappingException {
        return null;
    }
}
