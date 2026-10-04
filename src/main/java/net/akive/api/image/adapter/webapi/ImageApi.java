package net.akive.api.image.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.image.adapter.webapi.request.UploadUrlCreateRequest;
import net.akive.api.image.adapter.webapi.response.UploadUrlCreateResponse;
import net.akive.api.image.application.provided.UploadUrlCreator;
import net.akive.api.image.application.provided.dto.UploadUrlCreateResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageApi {

    private final UploadUrlCreator uploadUrlCreator;

    @PostMapping("/upload-urls")
    public UploadUrlCreateResponse createUploadUrl(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody UploadUrlCreateRequest request
    ) {
        UploadUrlCreateResult result = uploadUrlCreator.create(request.to(userId));

        return UploadUrlCreateResponse.from(result);
    }
}
