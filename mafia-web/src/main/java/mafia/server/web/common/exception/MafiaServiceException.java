package mafia.server.web.common.exception;

import lombok.Getter;

import java.util.List;

public class MafiaServiceException extends RuntimeException {

    @Getter
    private final ErrorCode errorCode;
    /**
     * 국제화나 메시지에 파라미터 필요로 인해 메시지 키 방식을 사용할 경우
     * 쓸수 있도록 정의된 필드
     */
    private final List<Object> args;

    public MafiaServiceException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
        args = List.of();
    }

    public MafiaServiceException(String message, ErrorCode errorCode, Object... objects) {
        super(message);
        this.errorCode = errorCode;
        args = List.of(objects);
    }
}
