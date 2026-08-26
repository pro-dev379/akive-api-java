package net.akive.api.auth.adapter.login.persistence;

import net.akive.api.auth.adapter.login.persistence.entity.LegacyUserPasswordEntity;
import net.akive.api.auth.domain.login.LegacyUserPassword;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static net.akive.api.auth.domain.login.LegacyUserPasswordFixture.createLegacyUserPassword;
import static net.akive.api.user.domain.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;

class LegacyUserPasswordJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 회원_식별자로_레거시_패스워드를_조회한다() {
        User savedUser = userRepository.save(createUser());
        LegacyUserPassword legacyUserPassword = createLegacyUserPassword(savedUser);
        legacyUserPasswordJpaRepository.save(LegacyUserPasswordEntity.from(legacyUserPassword));

        Optional<LegacyUserPassword> found = legacyUserPasswordRepository.findByUserId(savedUser.getUserId());

        assertThat(found).contains(legacyUserPassword);
    }

    @Test
    void 레거시_패스워드가_없으면_빈_값을_반환한다() {
        Optional<LegacyUserPassword> found = legacyUserPasswordRepository.findByUserId(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void 회원_식별자로_레거시_패스워드를_삭제한다() {
        User savedUser = userRepository.save(createUser());
        LegacyUserPassword legacyUserPassword = createLegacyUserPassword(savedUser);
        legacyUserPasswordJpaRepository.save(LegacyUserPasswordEntity.from(legacyUserPassword));

        legacyUserPasswordRepository.deleteByUserId(savedUser.getUserId());

        Optional<LegacyUserPassword> found = legacyUserPasswordRepository.findByUserId(savedUser.getUserId());
        assertThat(found).isEmpty();
    }
}
