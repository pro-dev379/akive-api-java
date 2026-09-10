package net.akive.api.user.application.provided;

public interface UserNicknameChecker {

    boolean existsByNickname(String nickname);

    boolean existsByNicknameExcludingUser(String nickname, Long userId);
}
