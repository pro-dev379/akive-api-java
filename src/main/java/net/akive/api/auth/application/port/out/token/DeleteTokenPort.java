package net.akive.api.auth.application.port.out.token;

public interface DeleteTokenPort {

    void delete(Long userId);
}
