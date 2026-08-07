package net.akive.api.auth.application.login.required;

public interface DeleteLegacyUserPasswordPort {

    void deleteByUserId(Long userId);
}
