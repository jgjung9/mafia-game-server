package mafia.server.web.api.controller;

import lombok.extern.slf4j.Slf4j;
import mafia.server.web.api.exception.JwtRefreshException;
import mafia.server.web.common.exception.MafiaServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "mafia.server.web.api.controller")
public class ApiGlobalExceptionHandler {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public ApiResponse<Void> handleBadCredentialsException(BadCredentialsException e) {
        log.debug("Unauthorized: {}", e.getMessage(), e);
        return ApiResponse.unauthorized(null, "아이디 또는 비밀번호가 잘못되었습니다");
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AuthenticationException.class)
    public ApiResponse<Void> handleAuthenticationException(AuthenticationException e) {
        log.debug("Forbidden: {}", e.getMessage(), e);
        return ApiResponse.forbidden(null, "접근 권한이 없습니다");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException e) {
        ObjectError objectError = e.getBindingResult().getAllErrors().getFirst();
        log.debug("Request Body Bind Exception: {}", objectError);
        return ApiResponse.badRequest(null, objectError.getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(JwtRefreshException.class)
    public ApiResponse<Void> handleJwtRefreshException(JwtRefreshException e) {
        log.debug("Invalid token refresh request: {}", e.getMessage());
        return ApiResponse.badRequest(null, "잘못된 토큰 갱신 요청입니다");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MafiaServiceException.class)
    public ApiResponse<Void> handleMafiaServiceException(MafiaServiceException e) {
        log.debug("MafiaServiceException: {}", e.getMessage());
        return ApiResponse.badRequest(null, e.getErrorCode().getMsgKey());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.warn("api error occurred", e);
        return ApiResponse.internalServerError(null, "서버에 알 수 없는 예외가 발생했습니다");
    }
}
