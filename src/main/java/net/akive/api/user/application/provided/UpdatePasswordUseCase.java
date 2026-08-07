package net.akive.api.user.application.provided;

public interface UpdatePasswordUseCase {

    void updatePassword(Long userId, String encodedPassword);
}
