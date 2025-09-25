package mafia.server.web.api.command;

import mafia.server.web.WebProtocol.Response;
import mafia.server.web.auth.AccountContext;

import java.time.LocalDateTime;

import static mafia.server.web.WebProtocol.Request;

/**
 * 정의 되지 않은 커맨드로 요청이 온 경우 사용
 * Component 로 등록하지 않고 CommandManager 에서 들고 있으면서 참조할 수 있는 Command 가 없을 때 반환
 */
public class UnknownCommand implements WebProtocolCommand {

    @Override
    public Request.Command getCommandType() {
        return Request.Command.UNKNOWN;
    }

    @Override
    public Response execute(AccountContext context, Request request, LocalDateTime now) {
        return Response.newBuilder()
                .setCommand(getCommandType())
                .setResult(Response.Result.FAILURE)
                .setMessage("invalid request")
                .build();
    }
}
