package mafia.server.web.common.exception;

import lombok.Getter;

public class MafiaServiceException extends RuntimeException {

    @Getter
    private final ErrorCode errorCode;

    public MafiaServiceException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
