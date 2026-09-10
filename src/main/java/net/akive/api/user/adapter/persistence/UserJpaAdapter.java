package net.akive.api.user.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.user.adapter.persistence.entity.UserEntity;
import net.akive.api.user.application.required.UserRepository;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class UserJpaAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        return userJpaRepository.save(UserEntity.from(user)).to();
    }

    @Override
    public Optional<User> findByUserIdAndNotWithdrawn(Long userId) {
        return userJpaRepository.findByUserIdAndWithdrawal_WithdrawnAtIsNull(userId).map(UserEntity::to);
    }

    @Override
    public Optional<User> findByEmailAndNotWithdrawn(Email email) {
        return userJpaRepository.findByEmailAndWithdrawal_WithdrawnAtIsNull(email.value()).map(UserEntity::to);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return userJpaRepository.existsByEmail(email.value());
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userJpaRepository.existsByUserDetail_Nickname(nickname);
    }

    @Override
    public boolean existsByNicknameNotAndUserIdNot(String nickname, Long userId) {
        return userJpaRepository.existsByUserDetail_NicknameAndUserIdNot(nickname, userId);
    }
}
