package net.akive.api.attendance.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.attendance.adapter.webapi.request.AttendanceProofSubmitRequest;
import net.akive.api.attendance.adapter.webapi.response.AttendanceProofSubmitResponse;
import net.akive.api.attendance.application.provided.AttendanceProofSubmitter;
import net.akive.api.attendance.domain.AttendanceProof;
import net.akive.api.core.config.security.AuthenticatedUserId;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/attendance-proofs")
@RequiredArgsConstructor
public class AttendanceProofApi {

    private final AttendanceProofSubmitter attendanceProofSubmitter;

    @PostMapping
    @ResponseStatus(CREATED)
    public AttendanceProofSubmitResponse submit(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody AttendanceProofSubmitRequest request
    ) {
        AttendanceProof attendanceProof = attendanceProofSubmitter.submit(request.to(userId));

        return AttendanceProofSubmitResponse.from(attendanceProof);
    }
}
