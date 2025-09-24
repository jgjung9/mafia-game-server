package mafia.server.web.api.command;

import mafia.server.web.WebProtocol.Request;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommandManager {
    private final Map<Request.Command, WebProtocolCommand> commandMap = new HashMap<>();
    private final WebProtocolCommand defaultCommand = new UnknownCommand();

    public CommandManager(List<WebProtocolCommand> commands) {
        for (WebProtocolCommand command : commands) {
            commandMap.put(command.getCommandType(), command);
        }
    }

    public WebProtocolCommand getCommand(Request request) {
        return commandMap.getOrDefault(request.getCommand(), defaultCommand);
    }

}
