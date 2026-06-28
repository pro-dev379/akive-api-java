package net.akive.api.user.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.user.application.port.in.UpdatePasswordUseCase;
import net.akive.api.user.application.port.out.LoadUserPort;
import net.akive.api.user.application.port.out.SaveUserPort;
import net.akive.api.user.domain.User;
import net.akive.api.user.excpetion.UserException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.user.excpetion.UserErrorCode.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AccountService implements UpdatePasswordUseCase {

    private final SaveUserPort saveUserPort;
    private final LoadUserPort loadUserPort;

    @Override
    @Transactional
    public void updatePassword(Long userId, String encodedPassword) {
        User user = loadUserPort.findByUserIdAndLeaveFalse(userId)
                .orElseThrow(() -> new UserException(NOT_FOUND));

        user.updatePassword(encodedPassword);

        saveUserPort.save(user);
    }
}
