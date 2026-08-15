package net.akive.api.common.domain;

import org.springframework.lang.NonNull;

import java.util.UUID;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record ClientInstanceId(UUID value) {

    public ClientInstanceId {
        requireNonNull(value);
    }

    public static ClientInstanceId parse(String value) {
        return new ClientInstanceId(UUID.fromString(value));
    }

    @NonNull
    @Override
    public String toString() {
        return value.toString();
    }
}
