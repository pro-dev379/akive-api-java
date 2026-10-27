package net.akive.api.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WithdrawalReason {

    INCONVENIENT_TO_USE("사용하기 불편해요"),
    MISSING_DESIRED_FEATURES("원하는 기능이 없어요"),
    NO_LONGER_NEEDED("서비스 필요성을 못 느껴요"),
    WANT_NEW_ACCOUNT("새 계정을 만들고 싶어요"),
    OTHER("기타"),
    ;

    private final String label;
}
