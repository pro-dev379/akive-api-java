package net.akive.api.auth.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.akive.api.auth.application.dto.result.token.ReissueTokenResult;
import org.springframework.lang.Nullable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record ReissueTokenResponse(String accessToken, @Nullable String refreshToken) {

    public static ReissueTokenResponse from(ReissueTokenResult reissueTokenResult) {
        return new ReissueTokenResponse(reissueTokenResult.accessToken(), reissueTokenResult.refreshToken());
    }
}
