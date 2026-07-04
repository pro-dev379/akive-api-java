package net.akive.api.auth.infrastructure.login;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.port.out.login.LoadEmailLoginUserPort;
import net.akive.api.auth.domain.login.LoginUser;
import net.akive.api.auth.domain.login.exception.LoginException;
import net.akive.api.user.infrastructure.UserJpaRepository;
import org.springframework.stereotype.Repository;

import static net.akive.api.auth.domain.login.exception.LoginErrorCode.INVALID_EMAIL_OR_PASSWORD;

@Repository
@RequiredArgsConstructor
class LoginUserJpaAdapter implements LoadEmailLoginUserPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public LoginUser loadByEmailAndLeaveFalse(String email) {
        return userJpaRepository.findByEmailAndLeaveFalse(email)
                .map(user -> new LoginUser(
                        user.getUserId(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getRoleType()
                ))
                .orElseThrow(() -> new LoginException(INVALID_EMAIL_OR_PASSWORD));
    }
}
