package net.akive.api.user.application.port.in;

public interface UpdatePasswordUseCase {

    void updatePassword(Long userId, String encodedPassword);
}
