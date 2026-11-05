package net.akive.api.common.cache;

import lombok.NoArgsConstructor;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.analytics.TimeSeriesUnit;
import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.legal.domain.LegalDocumentActionContext;
import net.akive.api.legal.domain.LegalDocumentType;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class CacheKeyProvider {

    public static String emailVerification(String email, EmailVerificationPurpose verificationPurpose) {
        return "email:%s:verification:%s".formatted(email, verificationPurpose.getCode());
    }

    public static String refreshToken(Long userId, ClientInstanceId clientInstanceId) {
        return "user:%s:client:%s:refresh-token".formatted(userId, clientInstanceId);
    }

    public static String refreshTokenPattern(Long userId) {
        return "user:%s:client:*:refresh-token".formatted(userId);
    }

    public static String coachingConversationLock(Long coachingConversationId, Long userId) {
        return "user:%s:coaching-conversation:%s:lock".formatted(userId, coachingConversationId);
    }

    public static String pendingCoachingResponse(Long coachingConversationId) {
        return "coaching-conversation:%s:pending-response".formatted(coachingConversationId);
    }

    public static String dailyLogTypes() {
        return "daily-log:types";
    }

    public static String withdrawalReasons() {
        return "withdrawal-reason:list";
    }

    public static String legalDocument(Long legalDocumentId) {
        return "legal-document:%s".formatted(legalDocumentId);
    }

    public static String currentLegalDocument(LegalDocumentType documentType) {
        return "legal-document:type:%s:current".formatted(documentType);
    }

    public static String legalDocumentHistoryHeaders(LegalDocumentType documentType) {
        return "legal-document:type:%s:history:headers".formatted(documentType);
    }

    public static String currentLegalDocumentHeaders(LegalDocumentActionContext actionContext) {
        return "legal-document:action-context:%s:current:headers".formatted(actionContext);
    }

    public static String dailyLogTimeUsageRates(Long userId, LocalDate startOn, LocalDate endOn) {
        return "user:%s:daily-log-analytics:time-usage-rates:%s:%s"
                .formatted(userId, startOn, endOn);
    }

    public static String dailyLogFocusMetrics(Long userId, LocalDate startOn, LocalDate endOn) {
        return "user:%s:daily-log-analytics:focus-metrics:%s:%s"
                .formatted(userId, startOn, endOn);
    }

    public static String dailyLogFocusSeries(
            Long userId,
            LocalDate startOn,
            LocalDate endOn,
            TimeSeriesUnit unit
    ) {
        return "user:%s:daily-log-analytics:focus-series:%s:%s:%s"
                .formatted(userId, startOn, endOn, unit);
    }

    public static String dailyLogAnalyticsPattern(Long userId) {
        return "user:%s:daily-log-analytics:*".formatted(userId);
    }

    public static String sessionalPlanCompletionRate(Long userId, LocalDate startOn, LocalDate endOn) {
        return "user:%s:sessional-plan-analytics:completion-rate:%s:%s"
                .formatted(userId, startOn, endOn);
    }

    public static String sessionalPlanCompletionRateSeries(
            Long userId,
            LocalDate startOn,
            LocalDate endOn,
            TimeSeriesUnit unit
    ) {
        return "user:%s:sessional-plan-analytics:completion-rate-series:%s:%s:%s"
                .formatted(userId, startOn, endOn, unit);
    }

    public static String sessionalPlanAnalyticsPattern(Long userId) {
        return "user:%s:sessional-plan-analytics:*".formatted(userId);
    }
}
