package mafia.server.web.api.controller;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
@ToString
public class ApiResponse<T> {
    private final int status;
    private final T body;
    private final String errorCode;
    private final String message;
    private final Instant timestamp;

    @Builder
    private ApiResponse(int status, T body, String errorCode, String message) {
        this.status = status;
        this.body = body;
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> response(int status, T body, String errorCode, String message) {
        return ApiResponse.<T>builder()
                .status(status)
                .body(body)
                .errorCode(errorCode)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return response(HttpStatus.OK.value(), data, null, null);
    }

    public static <T> ApiResponse<T> created() {
        return created(null);
    }

    public static <T> ApiResponse<T> created(T data) {
        return response(HttpStatus.CREATED.value(), data, null, null);
    }

    public static <T> ApiResponse<T> badRequest(String errorCode, String message) {
        return response(HttpStatus.BAD_REQUEST.value(), null, errorCode, message);
    }

    public static <T> ApiResponse<T> notFound(String errorCode, String message) {
        return response(HttpStatus.NOT_FOUND.value(), null, errorCode, message);
    }

    public static <T> ApiResponse<T> unauthorized(String errorCode, String message) {
        return response(HttpStatus.UNAUTHORIZED.value(), null, errorCode, message);
    }

    public static <T> ApiResponse<T> forbidden(String errorCode, String message) {
        return response(HttpStatus.FORBIDDEN.value(), null, errorCode, message);
    }

    public static <T> ApiResponse<T> internalServerError(String errorCode, String message) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, errorCode, message);
    }
}
