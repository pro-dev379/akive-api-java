package net.akive.api.auth.application.token.required;

import java.util.Optional;

public interface LoadTokenPort {

    Optional<String> loadByUserId(Long userId);
}
