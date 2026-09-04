package net.akive.api.common.exception.business;

public class BusinessRuleViolationException extends BusinessException {

    public BusinessRuleViolationException(BusinessRuleViolationError errorCode) {
        super(errorCode);
    }
}
