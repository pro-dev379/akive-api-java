package net.akive.api.auth.infrastructure.adapter.login;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.port.out.login.LoadEmailLoginUserPort;
import net.akive.api.auth.domain.login.LoginUser;
import net.akive.api.common.exception.business.UnauthorizedException;
import net.akive.api.common.domain.Email;
import net.akive.api.user.infrastructure.repository.UserJpaRepository;
import org.springframework.stereotype.Repository;

import static net.akive.api.common.exception.business.UnauthorizedError.INVALID_EMAIL_OR_PASSWORD;

@Repository
@RequiredArgsConstructor
class LoginUserJpaAdapter implements LoadEmailLoginUserPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public LoginUser loadByEmailAndLeaveFalse(Email email) {
        return userJpaRepository.findByEmailAndLeaveFalse(email.value())
                .map(user -> LoginUser.create(
                        user.getUserId(),
                        new Email(user.getEmail()),
                        user.getPassword(),
                        user.getRoleType()
                ))
                .orElseThrow(() -> new UnauthorizedException(INVALID_EMAIL_OR_PASSWORD));
    }
}
