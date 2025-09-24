package mafia.server.web.api.controller;

import lombok.extern.slf4j.Slf4j;
import mafia.server.web.WebProtocol;
import mafia.server.web.common.annotation.ExecutionTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/web/api")
public class WebProtocolController {

    @ExecutionTime
    @PostMapping(
            consumes = {"application/x-protobuf", "application/json"},
            produces = {"application/x-protobuf", "application/json"}
    )
    public ResponseEntity<WebProtocol.Response> webRpcRequest(WebProtocol.Request request) {
        log.info("webRpcRequest request={}", request.toString());

        // 로직 실행
        WebProtocol.Response response = WebProtocol.Response.getDefaultInstance();

        log.info("webRpcRequest response={}", response);
        return ResponseEntity.ok(response);
    }
}
