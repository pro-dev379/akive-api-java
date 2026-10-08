package net.akive.api.user.adapter.generation;

import net.akive.api.user.domain.UserRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static net.akive.api.user.domain.UserPolicy.NICKNAME_MAX_LENGTH;
import static net.akive.api.user.domain.UserPolicy.ROLE_BASED_NICKNAME_SUFFIX_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;

class RoleBasedNicknameGeneratorTest {

    private final RoleBasedNicknameGenerator roleBasedNicknameGenerator = new RoleBasedNicknameGenerator();

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void 역할_기반_닉네임을_생성한다(UserRole role) {
        String nickname = roleBasedNicknameGenerator.generate(role);
        String prefix = role.name().toLowerCase() + "@";

        assertThat(nickname).startsWith(prefix);
        assertThat(nickname).hasSize(prefix.length() + ROLE_BASED_NICKNAME_SUFFIX_LENGTH);
        assertThat(nickname).hasSizeLessThanOrEqualTo(NICKNAME_MAX_LENGTH);
        assertThat(nickname.substring(prefix.length()))
                .matches("[0-9a-f]{" + ROLE_BASED_NICKNAME_SUFFIX_LENGTH + "}");
    }
}
