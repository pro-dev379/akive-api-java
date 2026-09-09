package net.akive.api.auth.adapter.login.persistence;

import net.akive.api.auth.adapter.login.persistence.entity.LegacyUserPasswordEntity;
import net.akive.api.auth.domain.login.LegacyUserPassword;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static net.akive.api.auth.domain.login.LegacyUserPasswordFixture.createLegacyUserPassword;
import static net.akive.api.user.domain.UserFixture.UNKNOWN_USER_ID;
import static net.akive.api.user.domain.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;

class LegacyUserPasswordJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 회원_식별자로_레거시_패스워드를_조회한다() {
        User savedUser = userRepository.save(createUser());
        LegacyUserPassword legacyUserPassword = createLegacyUserPassword(savedUser);
        legacyUserPasswordJpaRepository.save(LegacyUserPasswordEntity.from(legacyUserPassword));
        flushAndClear();

        Optional<LegacyUserPassword> foundLegacyUserPassword =
                legacyUserPasswordRepository.findByUserId(savedUser.getUserId());

        assertThat(foundLegacyUserPassword).contains(legacyUserPassword);
    }

    @Test
    void 레거시_패스워드가_없으면_빈_값을_반환한다() {
        Optional<LegacyUserPassword> foundLegacyUserPassword =
                legacyUserPasswordRepository.findByUserId(UNKNOWN_USER_ID);

        assertThat(foundLegacyUserPassword).isEmpty();
    }

    @Test
    void 회원_식별자로_레거시_패스워드를_삭제한다() {
        User savedUser = userRepository.save(createUser());
        LegacyUserPassword legacyUserPassword = createLegacyUserPassword(savedUser);
        legacyUserPasswordJpaRepository.save(LegacyUserPasswordEntity.from(legacyUserPassword));
        flushAndClear();

        legacyUserPasswordRepository.deleteByUserId(savedUser.getUserId());
        flushAndClear();

        Optional<LegacyUserPassword> foundLegacyUserPassword =
                legacyUserPasswordRepository.findByUserId(savedUser.getUserId());
        assertThat(foundLegacyUserPassword).isEmpty();
    }
}
