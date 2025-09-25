package mafia.server.web.api.command;

import mafia.server.web.WebProtocol.Request;
import mafia.server.web.WebProtocol.Response;

import java.time.LocalDateTime;

public interface WebProtocolCommand {

    Request.Command getCommandType();

    Response execute(Request request, LocalDateTime now);
}
