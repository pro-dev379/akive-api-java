package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.application.required.LoadUserPort;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;

import static net.akive.api.common.exception.business.NotFoundError.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class QueryUserService implements UserFinder {

    private final LoadUserPort loadUserPort;

    @Override
    public User find(Long userId) {
        return loadUserPort.loadByUserIdAndLeaveFalse(userId).orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
    }
}
