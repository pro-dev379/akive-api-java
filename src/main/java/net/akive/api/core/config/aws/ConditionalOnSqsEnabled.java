package net.akive.api.core.config.aws;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Documented
@ConditionalOnProperty(
        name = "spring.cloud.aws.sqs.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public @interface ConditionalOnSqsEnabled {
}
