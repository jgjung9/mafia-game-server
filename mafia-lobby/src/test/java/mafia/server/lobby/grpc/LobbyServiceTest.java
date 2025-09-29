package mafia.server.lobby.grpc;

import io.grpc.Context;
import io.grpc.internal.testing.StreamRecorder;
import io.grpc.stub.StreamObserver;
import mafia.server.data.domain.game.user.User;
import mafia.server.data.domain.game.user.UserRepository;
import mafia.server.lobby.common.Constant;
import mafia.server.lobby.core.LobbyClient;
import mafia.server.lobby.core.LobbyClientManager;
import mafia.server.lobby.protocol.ClientConnect;
import mafia.server.lobby.protocol.LobbyClientMessage;
import mafia.server.lobby.protocol.LobbyServerMessage;
import mafia.server.lobby.protocol.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
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

        // when
        LobbyClientMessage clientMessage = LobbyClientMessage.newBuilder()
                .setConnect(ClientConnect.getDefaultInstance())
                .build();
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
}