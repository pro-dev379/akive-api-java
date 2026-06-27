package net.akive.api.auth.application.port.out.login;

public interface DeleteLegacyUserPasswordPort {

    void deleteByUserId(Long userId);
}
