package net.akive.api.core.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

class RecordSupportingTypeResolver extends ObjectMapper.DefaultTypeResolverBuilder {

    RecordSupportingTypeResolver(
            ObjectMapper.DefaultTyping typing,
            PolymorphicTypeValidator polymorphicTypeValidator
    ) {
        super(typing, polymorphicTypeValidator);
    }

    @Override
    public boolean useForType(JavaType type) {
        if (type.getRawClass().isRecord()) {
            return true;
        }

        return super.useForType(type);
    }
}
