package mafia.server.lobby.grpc;

import io.grpc.Context;
import io.grpc.internal.testing.StreamRecorder;
import io.grpc.stub.StreamObserver;
import mafia.server.data.domain.game.user.User;
import mafia.server.data.domain.game.user.UserRepository;
import mafia.server.lobby.common.Constant;
import mafia.server.lobby.core.LobbyClient;
import mafia.server.lobby.core.LobbyClientManager;
import mafia.server.lobby.core.UserDto;
import mafia.server.lobby.protocol.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.grpc.server.inprocess.exclusive=true")
class LobbyServiceTest {

    @Autowired
    private LobbyService lobbyService;
    @Autowired
    private LobbyClientManager lobbyClientManager;
    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
        lobbyClientManager.removeAll();
    }

    @Test
    @DisplayName("클라이언트와 연결을 진행한다")
    void clientConnect() throws Exception {
        // given
        Long accountId = 1L;
        String nickname = "testnick";
        Context context = saveUserAndReturnContext(accountId, nickname);
        StreamRecorder<LobbyServerMessage> responseObserver = StreamRecorder.create();

        LobbyClientMessage clientMessage = LobbyClientMessage.newBuilder()
                .setConnect(ClientConnect.getDefaultInstance())
                .build();

        // when
        context.run(() -> {
            StreamObserver<LobbyClientMessage> requestObserver = lobbyService.handleCommunication(responseObserver);
            requestObserver.onNext(clientMessage);
        });

        // then
        LobbyClient lobbyClient = lobbyClientManager.getClient(accountId).get();
        assertThat(lobbyClient.getAccountId()).isEqualTo(accountId);
        assertThat(lobbyClient.getUserStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(lobbyClient.getObserver()).isEqualTo(responseObserver);
        assertThat(lobbyClient.getUserDto().nickname()).isEqualTo(nickname);
    }

    @Test
    @DisplayName("로비서버의 유저의 상태를 변경한다")
    void setUserStatus() throws Exception {
        // given
        Long accountId = 1L;
        String nickname = "testnick";
        Context context = saveUserAndReturnContext(accountId, nickname);
        StreamRecorder<LobbyServerMessage> responseObserver = StreamRecorder.create();
        LobbyClient lobbyClient = createLobbyClient(accountId, nickname, UserStatus.PENDING, responseObserver);
        lobbyClientManager.addClient(accountId, lobbyClient);

        UserStatus userStatus = UserStatus.LOBBY;
        LobbyClientMessage clientMessage = LobbyClientMessage.newBuilder()
                .setSetUserStatus(ClientSetUserStatus.newBuilder()
                        .setUserStatus(userStatus)
                        .build()
                )
                .build();

        // when
        context.run(() -> {
            StreamObserver<LobbyClientMessage> requestObserver = lobbyService.handleCommunication(responseObserver);
            requestObserver.onNext(clientMessage);
        });

        // then
        assertThat(lobbyClient.getUserStatus()).isEqualTo(userStatus);
    }

    private Context saveUserAndReturnContext(Long accountId, String nickname) {
        userRepository.save(createUser(accountId, nickname));
        return Context.current().withValue(Constant.CLIENT_ID_CONTEXT_KEY, accountId.toString());
    }

    private User createUser(Long accountId, String nickname) {
        return User.builder()
                .accountId(accountId)
                .nickname(nickname)
                .build();
    }

    private LobbyClient createLobbyClient(Long accountId, String nickname, UserStatus status, StreamObserver<LobbyServerMessage> responseObserver) {
        LobbyClient lobbyClient = new LobbyClient(accountId, new UserDto(1L, nickname), responseObserver);
        lobbyClient.setUserStatus(status);
        return lobbyClient;
    }
}