package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.user.application.port.in.UpdatePasswordUseCase;
import net.akive.api.user.application.port.out.LoadUserPort;
import net.akive.api.user.application.port.out.SaveUserPort;
import net.akive.api.user.domain.User;
import net.akive.api.common.exception.business.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.NotFoundError.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AccountService implements UpdatePasswordUseCase {

    private final SaveUserPort saveUserPort;
    private final LoadUserPort loadUserPort;

    @Override
    @Transactional
    public void updatePassword(Long userId, String encodedPassword) {
        User user = loadUserPort.loadByUserIdAndLeaveFalse(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        user.updatePassword(encodedPassword);

        saveUserPort.save(user);
    }
}
