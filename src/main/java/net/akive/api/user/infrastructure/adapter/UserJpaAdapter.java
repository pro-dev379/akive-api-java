package net.akive.api.user.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.user.application.port.out.LoadUserPort;
import net.akive.api.user.application.port.out.SaveUserPort;
import net.akive.api.user.application.port.out.ValidateUserPort;
import net.akive.api.user.domain.User;
import net.akive.api.user.infrastructure.repository.UserJpaRepository;
import net.akive.api.user.infrastructure.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class UserJpaAdapter implements LoadUserPort, SaveUserPort, ValidateUserPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> loadByUserIdAndLeaveFalse(Long userId) {
        return userJpaRepository.findByUserIdAndLeaveFalse(userId).map(UserEntity::to);
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(UserEntity.from(user)).to();
    }

    @Override
    public boolean existsByUserIdAndLeaveFalse(Long userId) {
        return userJpaRepository.existsByUserIdAndLeaveFalse(userId);
    }

    @Override
    public boolean existsByEmailAndLeaveFalse(Email email) {
        return userJpaRepository.existsByEmailAndLeaveFalse(email.value());
    }

    @Override
    public boolean existsByEmail(Email email) {
        return userJpaRepository.existsByEmail(email.value());
    }
}
