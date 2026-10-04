package net.akive.api.attendance.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.attendance.adapter.webapi.request.AttendanceProofSubmitRequest;
import net.akive.api.attendance.adapter.webapi.response.AttendanceProofFindResponse;
import net.akive.api.attendance.adapter.webapi.response.AttendanceProofSubmitResponse;
import net.akive.api.attendance.application.provided.AttendanceProofFinder;
import net.akive.api.attendance.application.provided.AttendanceProofSubmitter;
import net.akive.api.attendance.domain.AttendanceProof;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.image.adapter.webapi.mapper.ImageResponseMapper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

import static net.akive.api.image.domain.VariantType.DETAIL;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/attendance-proofs")
@RequiredArgsConstructor
public class AttendanceProofApi {

    private final AttendanceProofSubmitter attendanceProofSubmitter;
    private final AttendanceProofFinder attendanceProofFinder;
    private final ImageResponseMapper imageResponseMapper;

    @PostMapping
    @ResponseStatus(CREATED)
    public AttendanceProofSubmitResponse submit(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody AttendanceProofSubmitRequest request
    ) {
        AttendanceProof attendanceProof = attendanceProofSubmitter.submit(request.to(userId));

        return AttendanceProofSubmitResponse.from(attendanceProof);
    }

    @GetMapping("/{proofOn}")
    public AttendanceProofFindResponse find(@AuthenticatedUserId Long userId, @PathVariable LocalDate proofOn) {
        AttendanceProof attendanceProof = attendanceProofFinder.findOrThrow(userId, proofOn);
        URI proofImageUrl = imageResponseMapper
                .resolveUrl(attendanceProof.getProofImageAsset(), DETAIL)
                .orElse(null);

        return AttendanceProofFindResponse.of(
                attendanceProof.getProofImageAsset().getProcessingStatus(),
                attendanceProof.getLate(),
                proofImageUrl
        );
    }
}
