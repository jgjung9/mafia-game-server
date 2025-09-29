package mafia.server.lobby.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mafia.server.lobby.common.Constant;
import mafia.server.lobby.core.LobbyClient;
import mafia.server.lobby.core.LobbyClientManager;
import mafia.server.lobby.core.UserDto;
import mafia.server.lobby.protocol.ClientConnect;
import mafia.server.lobby.protocol.LobbyClientMessage;
import mafia.server.lobby.protocol.LobbyServerMessage;
import mafia.server.lobby.protocol.LobbyServiceGrpc;
import mafia.server.lobby.service.UserService;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService(interceptors = JwtInterceptor.class)
@RequiredArgsConstructor
public class LobbyService extends LobbyServiceGrpc.LobbyServiceImplBase {

    private final LobbyClientManager lobbyClientManager;
    private final UserService userService;

    @Override
    public StreamObserver<LobbyClientMessage> handleCommunication(StreamObserver<LobbyServerMessage> responseObserver) {
        return new ClientStreamObserver(responseObserver, lobbyClientManager, userService);
    }

    @Slf4j
    @RequiredArgsConstructor
    private static class ClientStreamObserver implements StreamObserver<LobbyClientMessage> {

        private final StreamObserver<LobbyServerMessage> responseObserver;
        private final LobbyClientManager lobbyClientManager;
        private final UserService userService;


        @Override
        public void onNext(LobbyClientMessage lobbyClientMessage) {
            switch (lobbyClientMessage.getContentCase()) {
                case CONNECT -> handleConnect(lobbyClientMessage.getConnect());
            }
        }

        @Override
        public void onError(Throwable throwable) {
            log.info("Grpc LobbyService Error: {}", throwable.getMessage(), throwable);
            Long accountId = getAccountId();
            lobbyClientManager.removeClient(accountId);
        }

        @Override
        public void onCompleted() {
            lobbyClientManager.removeClient(getAccountId());
            responseObserver.onCompleted();
        }

        private void handleConnect(ClientConnect connect) {
            Long accountId = Long.valueOf(Constant.CLIENT_ID_CONTEXT_KEY.get());
            UserDto userDto = userService.findByAccountId(accountId);
            lobbyClientManager.addClient(accountId, new LobbyClient(accountId, userDto, responseObserver));
        }

        private Long getAccountId() {
            return Long.valueOf(Constant.CLIENT_ID_CONTEXT_KEY.get());
        }
    }
}
