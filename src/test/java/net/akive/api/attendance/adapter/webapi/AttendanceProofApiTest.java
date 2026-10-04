package net.akive.api.attendance.adapter.webapi;

import net.akive.api.attendance.adapter.webapi.request.AttendanceProofSubmitRequest;
import net.akive.api.attendance.application.provided.AttendanceProofSubmitter;
import net.akive.api.attendance.application.provided.dto.AttendanceProofSubmitCommand;
import net.akive.api.attendance.domain.AttendanceProof;
import net.akive.api.common.exception.business.BusinessRuleViolationError;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundError;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import static net.akive.api.attendance.adapter.webapi.fixture.AttendanceProofRequestFixture.createAttendanceProofSubmitRequest;
import static net.akive.api.attendance.domain.fixture.AttendanceProofFixture.*;
import static net.akive.api.common.exception.business.ResourceConflictError.ATTENDANCE_PROOF_ALREADY_EXISTS;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@WebMvcTest(AttendanceProofApi.class)
class AttendanceProofApiTest extends PrivateApiTestSupport {

    private static final String ATTENDANCE_PROOFS_URL = "/attendance-proofs";

    @MockitoBean
    AttendanceProofSubmitter attendanceProofSubmitter;

    @Test
    void 출석을_인증한다() {
        AttendanceProofSubmitRequest request = createAttendanceProofSubmitRequest();
        AttendanceProofSubmitCommand command = request.to(USER_ID);
        AttendanceProof attendanceProof = createReconstructedAttendanceProof(
                ATTENDANCE_PROOF_ID,
                createUser(),
                PROOF_ON,
                createAttendanceProofImageAsset(),
                true,
                PROOF_ON
        );
        given(attendanceProofSubmitter.submit(command)).willReturn(attendanceProof);

        MvcTestResultAssert result = postJson(
                ATTENDANCE_PROOFS_URL,
                USER_ID,
                toJson(request)
        ).hasStatus(CREATED);

        result.bodyJson().extractingPath("$.proofOn").isEqualTo(PROOF_ON.toString());
        result.bodyJson().extractingPath("$.late").isEqualTo(true);
        then(attendanceProofSubmitter).should().submit(command);
    }

    @Test
    void 출석_인증_이미지_식별자가_없으면_400을_응답한다() {
        AttendanceProofSubmitRequest request = new AttendanceProofSubmitRequest(null);

        postJson(ATTENDANCE_PROOFS_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        then(attendanceProofSubmitter).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @EnumSource(
            value = BusinessRuleViolationError.class,
            names = {
                    "ATTENDANCE_PROOF_IMAGE_NOT_UPLOADED",
                    "ATTENDANCE_PROOF_NOT_YET_ALLOWED"
            }
    )
    void 출석_인증_정책을_위반하면_400을_응답한다(BusinessRuleViolationError error) {
        AttendanceProofSubmitRequest request = createAttendanceProofSubmitRequest();
        AttendanceProofSubmitCommand command = request.to(USER_ID);
        willThrow(new BusinessRuleViolationException(error))
                .given(attendanceProofSubmitter)
                .submit(command);

        MvcTestResultAssert result = postJson(ATTENDANCE_PROOFS_URL, USER_ID, toJson(request))
                .hasStatus(BAD_REQUEST);

        assertErrorResponse(result, error);
        then(attendanceProofSubmitter).should().submit(command);
    }

    @ParameterizedTest
    @EnumSource(
            value = ResourceNotFoundError.class,
            names = {
                    "USER_NOT_FOUND",
                    "IMAGE_ASSET_NOT_FOUND",
                    "GOAL_NOT_FOUND"
            }
    )
    void 출석_인증에_필요한_리소스를_찾을_수_없으면_404를_응답한다(ResourceNotFoundError error) {
        AttendanceProofSubmitRequest request = createAttendanceProofSubmitRequest();
        AttendanceProofSubmitCommand command = request.to(USER_ID);
        willThrow(new ResourceNotFoundException(error))
                .given(attendanceProofSubmitter)
                .submit(command);

        MvcTestResultAssert result = postJson(ATTENDANCE_PROOFS_URL, USER_ID, toJson(request))
                .hasStatus(NOT_FOUND);

        assertErrorResponse(result, error);
        then(attendanceProofSubmitter).should().submit(command);
    }

    @Test
    void 같은_서비스일에_이미_출석을_인증했으면_409를_응답한다() {
        AttendanceProofSubmitRequest request = createAttendanceProofSubmitRequest();
        AttendanceProofSubmitCommand command = request.to(USER_ID);
        willThrow(new ResourceConflictException(ATTENDANCE_PROOF_ALREADY_EXISTS))
                .given(attendanceProofSubmitter)
                .submit(command);

        MvcTestResultAssert result = postJson(ATTENDANCE_PROOFS_URL, USER_ID, toJson(request))
                .hasStatus(CONFLICT);

        assertErrorResponse(result, ATTENDANCE_PROOF_ALREADY_EXISTS);
        then(attendanceProofSubmitter).should().submit(command);
    }
}
