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
    ACCOUNT_EXISTS("AUTH-001", "이미 존재하는 유저");

    private final String code;
    private final String msgKey;
}
