package mafia.server.lobby.grpc;

import io.grpc.CallCredentials;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.internal.testing.StreamRecorder;
import io.grpc.stub.StreamObserver;
import mafia.server.data.domain.game.user.User;
import mafia.server.data.domain.game.user.UserRepository;
import mafia.server.lobby.common.Constant;
import mafia.server.lobby.core.LobbyClient;
import mafia.server.lobby.core.LobbyClientManager;
import mafia.server.lobby.core.UserDto;
import mafia.server.lobby.core.room.RoomManager;
import mafia.server.lobby.protocol.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.grpc.test.AutoConfigureInProcessTransport;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@AutoConfigureInProcessTransport
class LobbyServiceIntegrationTest {

    @Autowired
    private GrpcChannelFactory grpcChannelFactory;
    @Autowired
    private LobbyClientManager lobbyClientManager;
    @Autowired
    private RoomManager roomManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LobbyService lobbyService;
    private LobbyServiceGrpc.LobbyServiceStub stub;

    private final Long testAccountId = 1L;
    private final String testNickname = "testnick";
    private final String testToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiYWNjb3VudElkIjoxLCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwiZXhwIjozOTA2NjU3MjQ5fQ.bwWwSPmCa-4EmEc4pFRfUlyYZGFouomaQ_QRNnaB2bfYicLMgqwTv7sVQl9GBeFvd0NyeZBnooON1RWJFJa_gA";

    @BeforeEach
    void setUp() {
        ManagedChannel channel = grpcChannelFactory.createChannel("test");
        LobbyServiceGrpc.LobbyServiceStub asyncStub = LobbyServiceGrpc.newStub(channel);
        userRepository.save(createUser(testAccountId, testNickname));

        stub = asyncStub.withCallCredentials(new CallCredentials() {
            @Override
            public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
                Metadata metadata = new Metadata();
                metadata.put(Constant.AUTHORIZATION_METADATA_KEY, testToken);
                metadataApplier.apply(metadata);
            }
        });
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        userRepository.deleteAllInBatch();
        lobbyClientManager.removeAll();

        ManagedChannel channel = (ManagedChannel) stub.getChannel();
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            channel.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("클라이언트와 연결을 진행한다")
    void clientConnect() throws Exception {
        // given
        StreamRecorder<LobbyServerMessage> responseObserver = StreamRecorder.create();
        StreamObserver<LobbyClientMessage> requestObserver = stub.handleCommunication(responseObserver);

        LobbyClientMessage clientMessage = LobbyClientMessage.newBuilder()
                .setConnect(ClientConnect.getDefaultInstance())
                .build();

        // when
        requestObserver.onNext(clientMessage);
        await()
                .during(Duration.ofMillis(1000))
                .until(() -> lobbyClientManager.getClient(testAccountId).isPresent());

        // then
        LobbyClient lobbyClient = lobbyClientManager.getClient(testAccountId).get();
        assertThat(lobbyClient.getAccountId()).isEqualTo(testAccountId);
        assertThat(lobbyClient.getUserStatus()).isEqualTo(UserStatus.PENDING);
        requestObserver.onCompleted();
    }

    @Test
    @DisplayName("클라이언트의 상태를 변경할 수 있다")
    void clientSetUserStatus() throws Exception {
        // given
        StreamRecorder<LobbyServerMessage> responseObserver = StreamRecorder.create();
        StreamObserver<LobbyClientMessage> requestObserver = stub.handleCommunication(responseObserver);

        LobbyClientMessage connectMessage = LobbyClientMessage.newBuilder()
                .setConnect(ClientConnect.getDefaultInstance())
                .build();
        LobbyClientMessage setUserStatusMessage = LobbyClientMessage.newBuilder()
                .setSetUserStatus(ClientSetUserStatus.newBuilder()
                        .setUserStatus(UserStatus.LOBBY)
                        .build())
                .build();

        // when
        requestObserver.onNext(connectMessage);
        requestObserver.onNext(setUserStatusMessage);
        await()
                .during(Duration.ofMillis(1000))
                .until(() -> {
                    if (lobbyClientManager.getClient(testAccountId).isEmpty()) {
                        return false;
                    }
                    return lobbyClientManager.getClient(testAccountId).get().getUserStatus() == UserStatus.LOBBY;
                });

        // then
        LobbyClient lobbyClient = lobbyClientManager.getClient(testAccountId).get();
        assertThat(lobbyClient.getAccountId()).isEqualTo(testAccountId);
        assertThat(lobbyClient.getUserStatus()).isEqualTo(UserStatus.LOBBY);
        requestObserver.onCompleted();
    }

    @Test
    @DisplayName("현재 로비에 접속해 있는 사람들에게 채팅 메시지를 보낼 수 있다")
    void clientChatAll() throws Exception {
        // given
        StreamRecorder<LobbyServerMessage> responseObserver = StreamRecorder.create();
        StreamObserver<LobbyClientMessage> requestObserver = stub.handleCommunication(responseObserver);
        String message = "test message content";

        LobbyClientMessage connectMessage = LobbyClientMessage.newBuilder()
                .setConnect(ClientConnect.getDefaultInstance())
                .build();
        LobbyClientMessage setUserStatusMessage = LobbyClientMessage.newBuilder()
                .setSetUserStatus(ClientSetUserStatus.newBuilder()
                        .setUserStatus(UserStatus.LOBBY)
                        .build())
                .build();

        LobbyClientMessage chatAllMessage = LobbyClientMessage.newBuilder()
                .setChatAll(ClientChatAll.newBuilder()
                        .setMessage(message)
                        .build())
                .build();

        // when
        requestObserver.onNext(connectMessage);
        requestObserver.onNext(setUserStatusMessage);
        requestObserver.onNext(chatAllMessage);
        await()
                .during(Duration.ofMillis(1000))
                .until(() -> {
                    if (lobbyClientManager.getClient(testAccountId).isEmpty()) {
                        return false;
                    }
                    if (lobbyClientManager.getClient(testAccountId).get().getUserStatus() != UserStatus.LOBBY) {
                        return false;
                    }
                    return !responseObserver.getValues().isEmpty();
                });

        // then
        for (LobbyServerMessage serverMessage : responseObserver.getValues()) {
            if (serverMessage.getContentCase().equals(LobbyServerMessage.ContentCase.CHAT_ALL)) {
                ServerChatAll chatAll = serverMessage.getChatAll();
                assertThat(chatAll.getAccountId()).isEqualTo(testAccountId);
                assertThat(chatAll.getNickname()).isEqualTo(testNickname);
                assertThat(chatAll.getMessage()).isEqualTo(message);
            }
        }
        requestObserver.onCompleted();
    }

    @Test
    @DisplayName("유저에게 개인 메시지를 보낼 수 있다")
    void clientChatDirect() throws Exception {
        // given
        // 테스트 기본 유저 설정 (DM을 받는 유저)
        StreamRecorder<LobbyServerMessage> responseObserver = StreamRecorder.create();
        StreamObserver<LobbyClientMessage> requestObserver = stub.handleCommunication(responseObserver);
        LobbyClientMessage connectMessage = LobbyClientMessage.newBuilder()
                .setConnect(ClientConnect.getDefaultInstance())
                .build();
        LobbyClientMessage setUserStatusMessage = LobbyClientMessage.newBuilder()
                .setSetUserStatus(ClientSetUserStatus.newBuilder()
                        .setUserStatus(UserStatus.LOBBY)
                        .build())
                .build();

        // 디엠을 보내는 유저 설정
        Long senderId = 2L;
        String senderNickname = "sender";
        String message = "test message content";
        StreamRecorder<LobbyServerMessage> senderResponseObserver = StreamRecorder.create();
        LobbyClientMessage chatDirectMessage = LobbyClientMessage.newBuilder()
                .setChatDirect(ClientChatDirect.newBuilder()
                        .setAccountId(testAccountId)
                        .setMessage(message)
                        .build())
                .build();
        lobbyClientManager.addClient(senderId, new LobbyClient(senderId, new UserDto(2L, senderNickname), senderResponseObserver, lobbyClientManager, roomManager));

        // when
        requestObserver.onNext(connectMessage);
        requestObserver.onNext(setUserStatusMessage);
        // 메시지를 받은 유저가 준비가 끝날때까지 기다린다
        await()
                .during(Duration.ofSeconds(1))
                .until(() -> {
                    if (lobbyClientManager.getClient(testAccountId).isEmpty()) {
                        return false;
                    }
                    return lobbyClientManager.getClient(testAccountId).get().getUserStatus() == UserStatus.LOBBY;
                });
        // 메시지를 보낸다
        Context context = Context.current().withValue(Constant.CLIENT_ID_CONTEXT_KEY, senderId.toString());
        context.call(() -> {
            StreamObserver<LobbyClientMessage> senderRequestObserver = lobbyService.handleCommunication(senderResponseObserver);
            senderRequestObserver.onNext(chatDirectMessage);
            return senderRequestObserver;
        });
        await()
                .during(Duration.ofSeconds(1))
                .until(() -> !responseObserver.getValues().isEmpty());

        // then
        for (LobbyServerMessage serverMessage : responseObserver.getValues()) {
            if (serverMessage.getContentCase().equals(LobbyServerMessage.ContentCase.CHAT_DIRECT)) {
                ServerChatDirect chatDirect = serverMessage.getChatDirect();
                assertThat(chatDirect.getAccountId()).isEqualTo(senderId);
                assertThat(chatDirect.getNickname()).isEqualTo(senderNickname);
                assertThat(chatDirect.getMessage()).isEqualTo(message);
            }
        }
        requestObserver.onCompleted();
    }

    @Test
    @DisplayName("로비에서 방을 만들 수 있다")
    void clientCreateRoom() throws Exception {
        // given
        StreamRecorder<LobbyServerMessage> responseObserver = StreamRecorder.create();
        StreamObserver<LobbyClientMessage> requestObserver = stub.handleCommunication(responseObserver);
        String title = "test room title";

        LobbyClientMessage connectMessage = LobbyClientMessage.newBuilder()
                .setConnect(ClientConnect.getDefaultInstance())
                .build();
        LobbyClientMessage setUserStatusMessage = LobbyClientMessage.newBuilder()
                .setSetUserStatus(ClientSetUserStatus.newBuilder()
                        .setUserStatus(UserStatus.LOBBY)
                        .build())
                .build();
        LobbyClientMessage createRoomMessage = LobbyClientMessage.newBuilder()
                .setCreateRoom(ClientCreateRoom.newBuilder()
                        .setTitle(title)
                        .build())
                .build();

        // when
        requestObserver.onNext(connectMessage);
        requestObserver.onNext(setUserStatusMessage);
        requestObserver.onNext(createRoomMessage);
        await()
                .during(Duration.ofSeconds(1))
                .until(() -> {
                    if (lobbyClientManager.getClient(testAccountId).isEmpty()) {
                        return false;
                    }
                    return lobbyClientManager.getClient(testAccountId).get().getUserStatus() == UserStatus.ROOM;
                });

        // then
        for (LobbyServerMessage serverMessage : responseObserver.getValues()) {
            if (serverMessage.getContentCase().equals(LobbyServerMessage.ContentCase.CREATE_ROOM_RESULT)) {
                ServerCreateRoomResult createRoomResult = serverMessage.getCreateRoomResult();
                assertThat(createRoomResult.getType()).isEqualTo(ServerCreateRoomResult.Type.SUCCESS);
                assertThat(createRoomResult.getRoomInfo().getTitle()).isEqualTo(title);
            }
        }
        requestObserver.onCompleted();
    }

    private User createUser(Long accountId, String nickname) {
        return User.builder()
                .accountId(accountId)
                .nickname(nickname)
                .build();
    }

}