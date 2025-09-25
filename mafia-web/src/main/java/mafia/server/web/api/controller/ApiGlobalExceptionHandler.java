package mafia.server.web.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "mafia.server.web.api.controller")
public class ApiGlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> validationExceptionHandler(BindException e) {
        ObjectError objectError = e.getBindingResult().getAllErrors().getFirst();
        log.debug("Request Body Bind Exception: {}", objectError);
        return ApiResponse.badRequest(null, objectError.getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> defaultExceptionHandler(Exception e) {
        log.warn("api error occurred", e);
        return ApiResponse.internalServerError(null, "서버에 알 수 없는 예외가 발생했습니다");
    }
}
