package net.akive.api.attendance.application.provided;

import net.akive.api.attendance.application.provided.dto.AttendanceProofSubmitCommand;
import net.akive.api.attendance.domain.AttendanceProof;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;

import java.time.LocalDate;

public interface AttendanceProofSubmitter {

    AttendanceProof submit(AttendanceProofSubmitCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException, ResourceConflictException;

    void cancel(Long userId, LocalDate proofOn) throws ResourceNotFoundException;
}
