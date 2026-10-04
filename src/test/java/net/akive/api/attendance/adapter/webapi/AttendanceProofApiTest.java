package net.akive.api.attendance.adapter.webapi;

import net.akive.api.attendance.adapter.webapi.request.AttendanceProofSubmitRequest;
import net.akive.api.attendance.application.provided.AttendanceProofFinder;
import net.akive.api.attendance.application.provided.AttendanceProofSubmitter;
import net.akive.api.attendance.application.provided.dto.AttendanceProofSubmitCommand;
import net.akive.api.attendance.domain.AttendanceProof;
import net.akive.api.common.exception.business.*;
import net.akive.api.image.adapter.webapi.mapper.ImageResponseMapper;
import net.akive.api.image.domain.ImageAsset;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.net.URI;
import java.util.Optional;

import static net.akive.api.attendance.adapter.webapi.fixture.AttendanceProofRequestFixture.createAttendanceProofSubmitRequest;
import static net.akive.api.attendance.domain.fixture.AttendanceProofFixture.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_INPUT_VALUE;
import static net.akive.api.common.exception.business.ResourceConflictError.ATTENDANCE_PROOF_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.ATTENDANCE_PROOF_NOT_FOUND;
import static net.akive.api.core.config.aws.fixture.CdnFixture.CDN_BASE_URL;
import static net.akive.api.image.domain.ProcessingStatus.READY;
import static net.akive.api.image.domain.ProcessingStatus.UPLOADING;
import static net.akive.api.image.domain.VariantType.DETAIL;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.RECIPE_VERSION;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.readyImageAsset;
import static net.akive.api.image.domain.fixture.ImageVariantFixture.FIRST_IMAGE_VARIANT_ID;
import static net.akive.api.image.domain.fixture.ImageVariantFixture.detailImageVariant;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(AttendanceProofApi.class)
class AttendanceProofApiTest extends PrivateApiTestSupport {

    private static final String ATTENDANCE_PROOFS_URL = "/attendance-proofs";
    private static final URI PROOF_IMAGE_URL = URI.create(CDN_BASE_URL + "/variants/attendance-proofs/1/detail.webp");

    @MockitoBean
    AttendanceProofSubmitter attendanceProofSubmitter;

    @MockitoBean
    AttendanceProofFinder attendanceProofFinder;

    @MockitoBean
    ImageResponseMapper imageResponseMapper;

    @Test
    void 출석_인증을_조회한다() {
        ImageAsset proofImageAsset = readyImageAsset(detailImageVariant(FIRST_IMAGE_VARIANT_ID, RECIPE_VERSION));
        AttendanceProof attendanceProof = createReconstructedAttendanceProof(proofImageAsset, true);
        given(attendanceProofFinder.findOrThrow(USER_ID, PROOF_ON)).willReturn(attendanceProof);
        given(imageResponseMapper.resolveUrl(attendanceProof.getProofImageAsset(), DETAIL))
                .willReturn(Optional.of(PROOF_IMAGE_URL));

        MvcTestResultAssert result = get(ATTENDANCE_PROOFS_URL + "/" + PROOF_ON, USER_ID).hasStatus(OK);

        result.bodyJson().extractingPath("$.proofImage.processingStatus").isEqualTo(READY.name());
        result.bodyJson().extractingPath("$.proofImage.url").isEqualTo(PROOF_IMAGE_URL.toString());
        result.bodyJson().extractingPath("$.late").isEqualTo(true);
        then(attendanceProofFinder).should().findOrThrow(USER_ID, PROOF_ON);
        then(imageResponseMapper).should().resolveUrl(attendanceProof.getProofImageAsset(), DETAIL);
    }

    @Test
    void 출석_인증_조회_시_출석_인증_이미지_에셋은_있지만_URL을_생성할_수_없으면_이미지_리사이즈_처리_상태와_null_URL을_응답한다() {
        AttendanceProof attendanceProof = createReconstructedAttendanceProof(false);
        given(attendanceProofFinder.findOrThrow(USER_ID, PROOF_ON)).willReturn(attendanceProof);
        given(imageResponseMapper.resolveUrl(attendanceProof.getProofImageAsset(), DETAIL))
                .willReturn(Optional.empty());

        MvcTestResultAssert result = get(ATTENDANCE_PROOFS_URL + "/" + PROOF_ON, USER_ID).hasStatus(OK);

        result.bodyJson().extractingPath("$.proofImage.processingStatus").isEqualTo(UPLOADING.name());
        result.bodyJson().extractingPath("$.proofImage.url").isNull();
        result.bodyJson().extractingPath("$.late").isEqualTo(false);
        then(attendanceProofFinder).should().findOrThrow(USER_ID, PROOF_ON);
        then(imageResponseMapper).should().resolveUrl(attendanceProof.getProofImageAsset(), DETAIL);
    }

    @Test
    void 출석_인증_조회_시_출석_인증이_없으면_404를_응답한다() {
        given(attendanceProofFinder.findOrThrow(USER_ID, PROOF_ON))
                .willThrow(new ResourceNotFoundException(ATTENDANCE_PROOF_NOT_FOUND));

        MvcTestResultAssert result = get(ATTENDANCE_PROOFS_URL + "/" + PROOF_ON, USER_ID)
                .hasStatus(NOT_FOUND);

        assertErrorResponse(result, ATTENDANCE_PROOF_NOT_FOUND);
        then(attendanceProofFinder).should().findOrThrow(USER_ID, PROOF_ON);
        then(imageResponseMapper).shouldHaveNoInteractions();
    }

    @Test
    void 출석_인증_조회_시_출석_인증일의_형식이_올바르지_않으면_400을_응답한다() {
        MvcTestResultAssert result = get(ATTENDANCE_PROOFS_URL + "/invalid-date", USER_ID)
                .hasStatus(BAD_REQUEST);

        assertErrorResponse(result, INVALID_INPUT_VALUE);
        result.bodyJson().extractingPath("$.errors[0].field").isEqualTo("proofOn");
        result.bodyJson()
                .extractingPath("$.errors[0].message")
                .isEqualTo("Invalid date format. Expected yyyy-MM-dd.");
        then(attendanceProofFinder).shouldHaveNoInteractions();
        then(imageResponseMapper).shouldHaveNoInteractions();
    }

    @Test
    void 출석을_인증한다() {
        AttendanceProofSubmitRequest request = createAttendanceProofSubmitRequest();
        AttendanceProofSubmitCommand command = request.to(USER_ID);
        AttendanceProof attendanceProof = createReconstructedAttendanceProof(true);
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
    void 출석_인증_이미지_에셋_식별자가_없으면_400을_응답한다() {
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
