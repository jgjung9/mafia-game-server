package mafia.server.web.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 마피아 웹 서버에서 발생한 에러 통합 처리 및 응답 메시지 설정
 * 국제화가 필요할 경우 메시지 키에 메시지가 아니라 키를 사용하는 방식으로 수정 필요
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    ACCOUNT_EXISTS("ACCOUNT-001", "이미 존재하는 유저"),
    ACCOUNT_NOT_FOUND("ACCOUNT-002", "존재하지 않는 계정입니다"),
    ACCOUNT_INVALID("ACCOUNT-003", "유효하지 않은 계정입니다"),


    // 계정 상태 관련
    ACCOUNT_INVALID_STATUS("ACCOUNT-010", "잘못된 계정 상태값입니다"),
    ACCOUNT_DELETED("ACCOUNT-011", "관리자에 의해 삭제된 계정입니다"),
    ACCOUNT_SUSPEND("ACCOUNT-012", "정지된 계정입니다"),
    ACCOUNT_WITHDRAWN("ACCOUNT-013", "탈퇴된 계정입니다"),
    ACCOUNT_DORMANT("ACCOUNT-014", "휴먼 계정입니다");


    private final String code;
    private final String msgKey;
}
