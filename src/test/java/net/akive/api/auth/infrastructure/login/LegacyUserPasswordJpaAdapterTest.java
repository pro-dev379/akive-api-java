package net.akive.api.auth.infrastructure.login;

import net.akive.api.auth.domain.login.LegacyUserPassword;
import net.akive.api.auth.infrastructure.entity.LegacyUserPasswordEntity;
import net.akive.api.user.application.port.out.SaveUserPort;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class LegacyUserPasswordJpaAdapterTest {

    @Autowired
    LegacyUserPasswordJpaAdapter legacyUserPasswordJpaAdapter;

    @Autowired
    SaveUserPort saveUserPort;

    @Autowired
    LegacyUserPasswordJpaRepository legacyUserPasswordJpaRepository;

    @Test
    void 회원의_기존_패스워드가_존재하면_조회할_수_있다() {
        // given
        User user = User.signUpByEmail(
                "test@test.com",
                "USER@abcde",
                "password123!",
                USER
        );

        User savedUser = saveUserPort.save(user);
        saveLegacyPassword(savedUser);

        // when
        Optional<LegacyUserPassword> result =
                legacyUserPasswordJpaAdapter.findByUserId(savedUser.getUserId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo(savedUser.getUserId());
        assertThat(result.get().hash()).isEqualTo("hashed-password");
        assertThat(result.get().salt()).isEqualTo("salt");
    }

    @Test
    void 회원의_기존_패스워드를_삭제할_수_있다() {
        // given
        User user = User.signUpByEmail(
                "test@test.com",
                "USER@abcde",
                "password123!",
                USER
        );

        User savedUser = saveUserPort.save(user);
        saveLegacyPassword(savedUser);

        // when
        legacyUserPasswordJpaAdapter.deleteByUserId(savedUser.getUserId());

        // then
        assertThat(
                legacyUserPasswordJpaAdapter.findByUserId(savedUser.getUserId())
        ).isEmpty();
    }

    private void saveLegacyPassword(User savedUser) {
        LegacyUserPassword legacyUserPassword = new LegacyUserPassword(savedUser.getUserId(), "hashed-password", "salt");
        legacyUserPasswordJpaRepository.save(LegacyUserPasswordEntity.fromModel(legacyUserPassword));
    }
}