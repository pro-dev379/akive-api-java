package net.akive.api.auth.application.token.required;

public interface DeleteTokenPort {

    void delete(Long userId);
}
