package net.akive.api.user.infrastructure;

import lombok.RequiredArgsConstructor;
import net.akive.api.user.application.port.out.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserJpaRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public boolean existsByUserIdAndLeaveFalse(Long userId) {
        return userJpaRepository.existsByUserIdAndLeaveFalse(userId);
    }

    @Override
    public boolean existsByEmailAndLeaveFalse(String email) {
        return userJpaRepository.existsByEmailAndLeaveFalse(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }
}
