package mafia.server.web.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mafia.server.web.WebProtocol;
import mafia.server.web.WebProtocol.Response;
import mafia.server.web.api.command.CommandManager;
import mafia.server.web.api.command.WebProtocolCommand;
import mafia.server.web.common.annotation.ExecutionTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/web/api")
public class WebProtocolController {

    private final CommandManager commandManager;

    @ExecutionTime
    @PostMapping(
            consumes = {"application/x-protobuf", "application/json"},
            produces = {"application/x-protobuf", "application/json"}
    )
    public ResponseEntity<Response> webRpcRequest(WebProtocol.Request request) {
        log.info("webRpcRequest request={}", request.toString());

        // 로직 실행
        WebProtocolCommand command = commandManager.getCommand(request);
        Response response = command.execute(request, LocalDateTime.now());

        log.info("webRpcRequest response={}", response);
        return ResponseEntity.ok(response);
    }
}
