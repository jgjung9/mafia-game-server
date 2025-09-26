package mafia.server.web.api.command;

import mafia.server.web.WebProtocol.Request;
import mafia.server.web.WebProtocol.Response;
import mafia.server.web.auth.AccountContext;

import java.time.LocalDateTime;

public interface WebProtocolCommand {

    Request.Command getCommandType();

    Response execute(AccountContext context, Request request, LocalDateTime now);
}
