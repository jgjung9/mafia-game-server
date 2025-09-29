package mafia.server.lobby.core;

import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mafia.server.lobby.protocol.LobbyServerMessage;
import mafia.server.lobby.protocol.UserStatus;

/**
 * 로비와 연결되는 유저(클라이언트)를 추상화한 클래스
 */
@Slf4j
@Getter
public class LobbyClient {

    private final Long accountId;
    private final UserDto userDto;
    private final StreamObserver<LobbyServerMessage> observer;
    @Setter
    private UserStatus userStatus;

    public LobbyClient(Long accountId, UserDto userDto, StreamObserver<LobbyServerMessage> observer) {
        this.accountId = accountId;
        this.userDto = userDto;
        this.observer = observer;
        userStatus = UserStatus.PENDING;
    }

    public void sendMessage(LobbyServerMessage serverMessage) {
        if (userStatus.equals(UserStatus.OFFLINE)) {
            return;
        }
        log.debug("SendServerMessage: accountId={}, serverMessage={}", accountId, serverMessage);
        observer.onNext(serverMessage);
    }

    public void close() {
        userStatus = UserStatus.OFFLINE;
    }
}
