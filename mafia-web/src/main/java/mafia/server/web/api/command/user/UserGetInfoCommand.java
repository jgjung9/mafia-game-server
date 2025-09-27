package mafia.server.web.api.command.user;

import lombok.RequiredArgsConstructor;
import mafia.server.data.domain.game.user.User;
import mafia.server.web.WebProtocol.Request;
import mafia.server.web.WebProtocol.Request.Command;
import mafia.server.web.WebProtocol.Response;
import mafia.server.web.WebProtocol.Response.Result;
import mafia.server.web.WebProtocol.UserGetInfoRes;
import mafia.server.web.WebProtocol.UserGetInfoRes.UserGetInfoResult;
import mafia.server.web.api.command.WebProtocolCommand;
import mafia.server.web.auth.AccountContext;
import mafia.server.web.common.protobuf.ProtobufUtils;
import mafia.server.web.service.game.UserService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserGetInfoCommand implements WebProtocolCommand {

    private final UserService userService;

    @Override
    public Command getCommandType() {
        return Command.USER_GET_INFO;
    }

    @Override
    public Response execute(AccountContext context, Request request, LocalDateTime now) {
        Response.Builder builder = Response.newBuilder()
                .setCommand(getCommandType())
                .setResult(Result.SUCCESS)
                .setTimestamp(ProtobufUtils.toTimestamp(now));

        User user = userService.findByAccountId(context.accountId());
        if (user == null) {
            return builder
                    .setUserGetInfoRes(createResOnlyResult(UserGetInfoResult.USER_NOT_FOUND))
                    .build();
        }

        return builder
                .setUserGetInfoRes(createRes(UserGetInfoResult.SUCCESS, user))
                .build();
    }

    private UserGetInfoRes createRes(UserGetInfoResult result, User user) {
        return UserGetInfoRes.newBuilder()
                .setUserGetInfoResult(result)
                .setNickname(user.getNickname())
                .setLevel(user.getLevel())
                .setExp(user.getExp())
                .build();
    }

    private UserGetInfoRes createResOnlyResult(UserGetInfoResult result) {
        return UserGetInfoRes.newBuilder()
                .setUserGetInfoResult(result)
                .build();
    }
}
