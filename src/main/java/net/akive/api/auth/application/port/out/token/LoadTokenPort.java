package net.akive.api.auth.application.port.out.token;

import java.util.Optional;

public interface LoadTokenPort {

    Optional<String> loadByUserId(Long userId);
}
