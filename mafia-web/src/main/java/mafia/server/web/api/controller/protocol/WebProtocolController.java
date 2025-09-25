package mafia.server.web.api.controller.protocol;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mafia.server.web.WebProtocol.Request;
import mafia.server.web.WebProtocol.Response;
import mafia.server.web.api.command.CommandManager;
import mafia.server.web.api.command.WebProtocolCommand;
import mafia.server.web.auth.AccountContext;
import mafia.server.web.auth.annotation.JwtContext;
import mafia.server.web.common.annotation.ExecutionTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 게임에서 유저의 행위를 기반으로 한 요청이므로 rest 방식이 아닌 rpc 방식을 이용해 구현
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/rpc")
public class WebProtocolController {

    private final CommandManager commandManager;

    @ExecutionTime
    @PostMapping(
            consumes = {"application/x-protobuf", "application/json"},
            produces = {"application/x-protobuf", "application/json"}
    )
    public ResponseEntity<Response> webRpcRequest(@JwtContext AccountContext accountContext, Request request) {
        log.info("webRpcRequest accountContext={}, request=\n{}", accountContext, request.toString());

        // 로직 실행
        WebProtocolCommand command = commandManager.getCommand(request);
        Response response = command.execute(accountContext, request, LocalDateTime.now());

        log.info("webRpcRequest response=\n{}", response);
        return ResponseEntity.ok(response);
    }
}
