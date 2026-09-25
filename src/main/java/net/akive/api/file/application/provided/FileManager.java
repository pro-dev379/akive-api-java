package net.akive.api.file.application.provided;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.file.application.provided.dto.UploadUrlCreateCommand;
import net.akive.api.file.application.provided.dto.UploadUrlCreateResult;

public interface FileManager {

    UploadUrlCreateResult createUploadUrl(UploadUrlCreateCommand command) throws BusinessRuleViolationException;
}
