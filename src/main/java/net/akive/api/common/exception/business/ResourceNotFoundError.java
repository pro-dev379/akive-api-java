package net.akive.api.common.exception.business;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.ApplicationError;
import net.akive.api.common.exception.ApplicationErrorType;

import static net.akive.api.common.exception.ApplicationErrorType.RESOURCE_NOT_FOUND;

@Getter
@RequiredArgsConstructor
public enum ResourceNotFoundError implements ApplicationError {

    ENDPOINT_NOT_FOUND("Endpoint not found."),

    USER_NOT_FOUND("User not found."),
    GOAL_NOT_FOUND("Goal not found."),

    SESSIONAL_PLAN_NOT_FOUND("Sessional plan not found."),
    TOTAL_QUANTITY_NOT_FOUND("Total quantity not found."),
    WEEKLY_QUANTITY_NOT_FOUND("Weekly quantity not found."),
    WEEKLY_PLAN_NOT_FOUND("Weekly plan not found."),
    WEEKLY_PLAN_ITEM_NOT_FOUND("Weekly plan item not found."),
    DAILY_QUANTITY_NOT_FOUND("Daily quantity not found"),

    EMAIL_VERIFICATION_NOT_FOUND("Email verification not found."),

    DAILY_LOG_NOT_FOUND("Daily log not found."),

    REPORT_NOT_FOUND("Report not found."),

    COACHING_CONVERSATION_NOT_FOUND("Coaching conversation not found."),

    IMAGE_ASSET_NOT_FOUND("Image asset not found."),

    ATTENDANCE_PROOF_NOT_FOUND("Attendance proof not found"),
    ;

    private final String message;

    @Override
    public ApplicationErrorType getType() {
        return RESOURCE_NOT_FOUND;
    }
}
