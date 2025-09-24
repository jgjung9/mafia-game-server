package mafia.server.web.api.controller;

import lombok.extern.slf4j.Slf4j;
import mafia.server.web.WebProtocol.Request;
import mafia.server.web.WebProtocol.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "mafia.server.web.api.controller")
public class ApiGlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> defaultExceptionHandler(Exception e) {
        log.warn("Error occurred: unknown error", e);
        return ResponseEntity.ok(Response.newBuilder()
                .setCommand(Request.Command.UNKNOWN)
                .setResult(Response.Result.FAILURE)
                .setMessage("internal server error")
                .build());
    }
}
