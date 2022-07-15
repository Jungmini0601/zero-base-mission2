package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("사용자가 없습니다."),
    INTERNAL_ERROR("예측하지 못한 에러가 발생 했습니다."),
    MAX_ACCOUNT_PER_USER_10("사용자당 최대 계좌 개수는 10개 입니다."),
    ACCOUNT_NOT_FOUND("계좌가 없습니다."),
    ACCOUNT_TRANSACTION_LOCK("해당 계좌는 사용 중 입니다."),
    AMOUNT_EXCEED_BALANCE("거래 금액이 계좌 잔액보다 큽니다."),
    ACCOUNT_ALREADY_UNREGISTERED("계좌가 이미 중지되었습니다"),
    TRANSACTION_NOT_FOUND("해당 거래가 없습니다"),
    BALANCE_NOT_EMPTY("잔액이 있는 계좌는 해지 할 수 없습니다."),
    USER_ACCOUNT_UN_MATCH("사용자와 계좌의 소유주가 다릅니다."),
    CANCEL_MUST_FULLY("거래는 전액 환불만 가능합니다"),
    TOO_OLD_ORDER_TO_CANCEL("1년이 지난 거래는 취소가 불가능 합니다"),
    TRANSACTION_ACCOUNT_UN_MATCH("거래한 계좌가 아닙니다."),
    INVALID_REQUEST("거래 취소 금액은 양수여야 합니다.");

    private final String description;
}
