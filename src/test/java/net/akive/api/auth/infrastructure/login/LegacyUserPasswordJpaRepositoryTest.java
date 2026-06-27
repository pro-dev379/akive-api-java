package net.akive.api.auth.infrastructure.login;

import net.akive.api.auth.domain.login.LegacyUserPassword;
import net.akive.api.auth.infrastructure.entity.LegacyUserPasswordEntity;
import net.akive.api.user.domain.User;
import net.akive.api.user.infrastructure.UserJpaRepository;
import net.akive.api.user.infrastructure.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static net.akive.api.user.domain.LoginType.EMAIL;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
class LegacyUserPasswordJpaRepositoryTest {

    @Autowired
    LegacyUserPasswordJpaRepository legacyUserPasswordJpaRepository;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Test
    void 회원_식별자로_레거시_패스워드를_조회할_수_있다() {
        // given
        UserEntity savedUser = saveUser();
        saveLegacyPassword(savedUser);

        // when
        Optional<LegacyUserPasswordEntity> result = legacyUserPasswordJpaRepository.findByUser_UserId(savedUser.getUserId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getUserId())
                .isEqualTo(savedUser.getUserId());
    }

    @Test
    void 회원의_레거시_패스워드가_없으면_조회되지_않는다() {
        // given
        Long userId = 999L;

        // when
        Optional<LegacyUserPasswordEntity> result = legacyUserPasswordJpaRepository.findByUser_UserId(userId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 회원의_레거시_패스워드를_삭제하면_더_이상_조회되지_않는다() {
        // given
        UserEntity savedUser = saveUser();
        saveLegacyPassword(savedUser);

        // when
        legacyUserPasswordJpaRepository.deleteByUser_UserId(savedUser.getUserId());

        // then
        Optional<LegacyUserPasswordEntity> result = legacyUserPasswordJpaRepository.findByUser_UserId(savedUser.getUserId());

        assertThat(result).isEmpty();
    }

    private UserEntity saveUser() {
        User user = User.builder()
                .email("test@test.com")
                .nickname("USER@abcde")
                .password("password123!")
                .roleType(USER)
                .loginType(EMAIL)
                .build();

        return userJpaRepository.save(UserEntity.fromModel(user));
    }

    private void saveLegacyPassword(UserEntity savedUser) {
        LegacyUserPassword legacyUserPassword = new LegacyUserPassword(savedUser.getUserId(), "hashed-password", "salt");
        legacyUserPasswordJpaRepository.save(LegacyUserPasswordEntity.fromModel(legacyUserPassword));
    }
}