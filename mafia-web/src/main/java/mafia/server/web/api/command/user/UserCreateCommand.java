package mafia.server.web.api.command.user;

import lombok.RequiredArgsConstructor;
import mafia.server.data.domain.game.user.User;
import mafia.server.data.domain.game.user.UserRepository;
import mafia.server.web.WebProtocol.Request;
import mafia.server.web.WebProtocol.Request.Command;
import mafia.server.web.WebProtocol.Response;
import mafia.server.web.WebProtocol.UserCreateRes;
import mafia.server.web.WebProtocol.UserCreateRes.UserCreateResult;
import mafia.server.web.api.command.WebProtocolCommand;
import mafia.server.web.auth.AccountContext;
import mafia.server.web.common.protobuf.ProtobufUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserCreateCommand implements WebProtocolCommand {

    private final UserRepository userRepository;

    @Override
    public Command getCommandType() {
        return Command.USER_CREATE;
    }

    @Override
    public Response execute(AccountContext context, Request request, LocalDateTime now) {
        Response.Builder builder = Response.newBuilder().setCommand(getCommandType())
                .setTimestamp(ProtobufUtils.toTimestamp(now))
                .setResult(Response.Result.SUCCESS);

        // 유저 존재 확인
        Long accountId = context.accountId();
        if (userRepository.existsByAccountId(accountId)) {
            return builder
                    .setUserCreateRes(createRes(UserCreateResult.EXISTS_USER))
                    .build();
        }

        // 닉네임 중복 확인
        String nickname = request.getUserCreateReq().getNickname();
        if (userRepository.existsByNickname(nickname)) {
            return builder
                    .setUserCreateRes(createRes(UserCreateResult.EXISTS_NICKNAME))
                    .build();
        }

        // 닉네임 유효성 체크
        if (!validateNickname(nickname)) {
            return builder
                    .setUserCreateRes(createRes(UserCreateResult.INVALID_NICKNAME))
                    .build();
        }

        userRepository.save(User.create(accountId, nickname));
        return builder
                .setUserCreateRes(createRes(UserCreateResult.SUCCESS))
                .build();
    }

    private boolean validateNickname(String nickname) {
        // 닉네임 정책이 생기면 로직 작성
        if (nickname.length() < 2 || nickname.length() > 12) {
            return false;
        }
        return true;
    }

    private UserCreateRes createRes(UserCreateResult result) {
        return UserCreateRes.newBuilder()
                .setUserCreateResult(result)
                .build();
    }
}
