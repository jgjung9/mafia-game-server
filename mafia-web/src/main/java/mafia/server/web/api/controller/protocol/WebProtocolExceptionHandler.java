package mafia.server.web.api.controller.protocol;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import mafia.server.web.WebProtocol.Request;
import mafia.server.web.WebProtocol.Response;
import mafia.server.web.common.exception.MafiaServiceException;
import mafia.server.web.common.protobuf.ProtobufUtils;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@Order(1)
@RestControllerAdvice(basePackages = "mafia.server.web.api.controller.protocol")
public class WebProtocolExceptionHandler {

    @ExceptionHandler(MafiaServiceException.class)
    public Response handleMafiaServiceException(HttpServletRequest servletRequest, MafiaServiceException e) {
        Request request = (Request) servletRequest.getAttribute("WebProtocol.Request");
        log.warn("MafiaServiceException: {}", e.getMessage());
        return Response.newBuilder()
                .setCommand(request.getCommand())
                .setResult(Response.Result.FAILURE)
                .setMessage(e.getMessage())
                .setTimestamp(ProtobufUtils.toTimestamp(LocalDateTime.now()))
                .build();
    }

    @ExceptionHandler(Exception.class)
    public Response defaultExceptionHandler(HttpServletRequest servletRequest, Exception e) {
        Request request = (Request) servletRequest.getAttribute("WebProtocol.Request");
        log.warn("Error occurred: unknown error", e);
        return Response.newBuilder()
                .setCommand(request.getCommand())
                .setResult(Response.Result.FAILURE)
                .setMessage("internal server error")
                .setTimestamp(ProtobufUtils.toTimestamp(LocalDateTime.now()))
                .build();
    }
}
