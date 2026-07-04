package net.akive.api.user.infrastructure;

import lombok.RequiredArgsConstructor;
import net.akive.api.user.application.port.out.LoadUserPort;
import net.akive.api.user.application.port.out.SaveUserPort;
import net.akive.api.user.application.port.out.ValidateUserPort;
import net.akive.api.user.domain.User;
import net.akive.api.user.infrastructure.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class UserJpaAdapter implements LoadUserPort, SaveUserPort, ValidateUserPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> loadByUserIdAndLeaveFalse(Long userId) {
        return userJpaRepository.findByUserIdAndLeaveFalse(userId).map(UserEntity::toModel);
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(UserEntity.fromModel(user)).toModel();
    }

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
