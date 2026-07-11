package net.akive.api.auth.infrastructure.login;

import net.akive.api.common.domain.Email;
import net.akive.api.auth.domain.login.LegacyUserPassword;
import net.akive.api.auth.infrastructure.entity.LegacyUserPasswordEntity;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;

class LegacyUserPasswordJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 회원의_기존_패스워드가_존재하면_조회할_수_있다() {
        // given
        User user = User.signUpByEmail(
                Email.of("test@test.com"),
                "USER@abcde",
                "password123!",
                USER
        );

        User savedUser = saveUserPort.save(user);
        saveLegacyPassword(savedUser);

        // when
        Optional<LegacyUserPassword> result = loadLegacyUserPasswordPort.loadByUserId(savedUser.getUserId());

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
                Email.of("test@test.com"),
                "USER@abcde",
                "password123!",
                USER
        );

        User savedUser = saveUserPort.save(user);
        saveLegacyPassword(savedUser);

        // when
        deleteLegacyUserPasswordPort.deleteByUserId(savedUser.getUserId());

        // then
        assertThat(loadLegacyUserPasswordPort.loadByUserId(savedUser.getUserId())).isEmpty();
    }

    private void saveLegacyPassword(User savedUser) {
        LegacyUserPassword legacyUserPassword = new LegacyUserPassword(savedUser.getUserId(), "hashed-password", "salt");
        legacyUserPasswordJpaRepository.save(LegacyUserPasswordEntity.from(legacyUserPassword));
    }
}