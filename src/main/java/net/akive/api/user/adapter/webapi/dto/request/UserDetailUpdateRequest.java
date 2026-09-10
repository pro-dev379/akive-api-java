package net.akive.api.user.adapter.webapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.akive.api.user.application.provided.dto.UserDetailUpdateCommand;

import static net.akive.api.user.domain.UserPolicy.AIM_MAX_LENGTH;
import static net.akive.api.user.domain.UserPolicy.NICKNAME_MAX_LENGTH;

public record UserDetailUpdateRequest(@NotNull @Valid UserDetailRequest targetUserDetail) {

    public UserDetailUpdateCommand to(Long userId) {
        return new UserDetailUpdateCommand(
                userId,
                this.targetUserDetail.nickname(),
                this.targetUserDetail.imgUrl(),
                this.targetUserDetail.aim()
        );
    }

    public record UserDetailRequest(
            @NotBlank @Size(max = NICKNAME_MAX_LENGTH) String nickname,
            String imgUrl,
            @Size(max = AIM_MAX_LENGTH) String aim
    ) {}
}
