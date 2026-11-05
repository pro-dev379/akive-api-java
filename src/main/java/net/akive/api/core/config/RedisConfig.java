package net.akive.api.core.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.ObservationRegistry;
import net.akive.api.core.config.observability.RemoteServiceNamingProperties;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ClientResourcesBuilderCustomizer lettuceTracingCustomizer(
            ObservationRegistry observationRegistry,
            RemoteServiceNamingProperties remoteServiceNamingProperties
    ) {
        return builder -> builder.tracing(new MicrometerTracing(
                observationRegistry,
                remoteServiceNamingProperties.redis()
        ));
    }

    @Bean
    public RedisTemplate<String, Object> customRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        PolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("net.akive")
                .build();

        RecordSupportingTypeResolver typeResolver =
                new RecordSupportingTypeResolver(ObjectMapper.DefaultTyping.NON_FINAL, validator);

        StdTypeResolverBuilder initializedResolver = typeResolver
                .init(JsonTypeInfo.Id.CLASS, null)
                .inclusion(JsonTypeInfo.As.PROPERTY);

        objectMapper.setDefaultTyping(initializedResolver);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

        return template;
    }
}
