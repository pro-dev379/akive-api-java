package net.akive.api.auth.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.lang.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenReissueResponse(String accessToken, @Nullable String refreshToken) {
}
