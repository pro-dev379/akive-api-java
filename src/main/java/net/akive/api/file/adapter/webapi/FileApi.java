package net.akive.api.file.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.file.adapter.webapi.request.UploadUrlCreateRequest;
import net.akive.api.file.adapter.webapi.response.UploadUrlCreateResponse;
import net.akive.api.file.application.provided.UploadUrlCreator;
import net.akive.api.file.application.provided.dto.UploadUrlCreateResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileApi {

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
