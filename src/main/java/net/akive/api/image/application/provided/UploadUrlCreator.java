package net.akive.api.image.application.provided;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.image.application.provided.dto.UploadUrlCreateCommand;
import net.akive.api.image.application.provided.dto.UploadUrlCreateResult;

public interface UploadUrlCreator {

    UploadUrlCreateResult create(UploadUrlCreateCommand command) throws BusinessRuleViolationException;
}
