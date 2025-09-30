package mafia.server.lobby.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mafia.server.lobby.common.Constant;
import mafia.server.lobby.common.ProtobufUtils;
import mafia.server.lobby.core.LobbyClient;
import mafia.server.lobby.core.LobbyClientManager;
import mafia.server.lobby.core.UserDto;
import mafia.server.lobby.core.room.Room;
import mafia.server.lobby.core.room.RoomManager;
import mafia.server.lobby.protocol.*;
import mafia.server.lobby.service.UserService;
import org.springframework.grpc.server.service.GrpcService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@GrpcService(interceptors = JwtInterceptor.class)
@RequiredArgsConstructor
public class LobbyService extends LobbyServiceGrpc.LobbyServiceImplBase {

    private final UserService userService;
    private final LobbyClientManager lobbyClientManager;
    private final RoomManager roomManager;

    @Override
    public StreamObserver<LobbyClientMessage> handleCommunication(StreamObserver<LobbyServerMessage> responseObserver) {
        return new ClientStreamObserver(responseObserver, userService, lobbyClientManager, roomManager);
    }

    @Slf4j
    @RequiredArgsConstructor
    private static class ClientStreamObserver implements StreamObserver<LobbyClientMessage> {

        private final StreamObserver<LobbyServerMessage> responseObserver;
        private final UserService userService;
        private final LobbyClientManager lobbyClientManager;
        private final RoomManager roomManager;


        @Override
        public void onNext(LobbyClientMessage lobbyClientMessage) {
            switch (lobbyClientMessage.getContentCase()) {
                case CONNECT -> handleConnect(lobbyClientMessage.getConnect());
                case SET_USER_STATUS -> handleSetUserStatus(lobbyClientMessage.getSetUserStatus());
                case CHAT_ALL -> handleChatAll(lobbyClientMessage.getChatAll());
                case CHAT_DIRECT -> handleChatDirect(lobbyClientMessage.getChatDirect());
                case CREATE_ROOM -> handleCreateRoom(lobbyClientMessage.getCreateRoom());
                case ENTER_ROOM -> handleEnterRoom(lobbyClientMessage.getEnterRoom());
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

        private Long getAccountId() {
            return Long.valueOf(Constant.CLIENT_ID_CONTEXT_KEY.get());
        }

        private void handleConnect(ClientConnect connect) {
            Long accountId = getAccountId();
            log.debug("handleConnect accountId={}", accountId);

            UserDto userDto = userService.findByAccountId(accountId);
            lobbyClientManager.addClient(accountId, new LobbyClient(accountId, userDto, responseObserver));
        }

        private void handleSetUserStatus(ClientSetUserStatus setUserStatus) {
            Long accountId = getAccountId();
            LocalDateTime now = LocalDateTime.now();
            log.debug("handleSetUserStatus accountId={}, setUserStatus={}", accountId, setUserStatus);

            LobbyClient client = lobbyClientManager.getClient(accountId)
                    .orElseThrow();
            updateUserStatus(client, setUserStatus.getUserStatus(), now);
        }

        private void handleChatAll(ClientChatAll chatAll) {
            Long accountId = getAccountId();
            LocalDateTime now = LocalDateTime.now();
            log.debug("handleChatAll accountId={}, chatAll={}", accountId, chatAll);

            UserDto userDto = lobbyClientManager.getClient(accountId)
                    .orElseThrow()
                    .getUserDto();

            LobbyServerMessage serverMessage = LobbyServerMessage.newBuilder()
                    .setTimestamp(ProtobufUtils.toTimestamp(now))
                    .setChatAll(ServerChatAll.newBuilder()
                            .setAccountId(accountId)
                            .setNickname(userDto.nickname())
                            .setMessage(chatAll.getMessage())
                            .build())
                    .build();

            lobbyClientManager.broadcastLobby(serverMessage);
        }

        private void handleChatDirect(ClientChatDirect chatDirect) {
            Long accountId = getAccountId();
            LocalDateTime now = LocalDateTime.now();
            log.debug("handleChatDirect accountId={}, chatDirect={}", accountId, chatDirect);

            UserDto userDto = lobbyClientManager.getClient(accountId)
                    .orElseThrow()
                    .getUserDto();

            LobbyServerMessage serverMessage = LobbyServerMessage.newBuilder()
                    .setTimestamp(ProtobufUtils.toTimestamp(now))
                    .setChatDirect(ServerChatDirect.newBuilder()
                            .setAccountId(accountId)
                            .setNickname(userDto.nickname())
                            .setMessage(chatDirect.getMessage())
                            .build())
                    .build();

            lobbyClientManager.sendMessage(chatDirect.getAccountId(), serverMessage);
        }

        private void handleCreateRoom(ClientCreateRoom createRoom) {
            Long accountId = getAccountId();
            LocalDateTime now = LocalDateTime.now();
            log.debug("handleCreateRoom accountId={}, createRoom={}", accountId, createRoom);

            LobbyClient client = lobbyClientManager.getClient(accountId)
                    .orElseThrow();
            if (!client.getUserStatus().equals(UserStatus.LOBBY)) {
                throw new IllegalStateException("Failed to create room: 유저가 로비에 있지 않습니다");
            }

            Room room = roomManager.createRoom(createRoom.getTitle(), client);
            updateUserStatus(client, UserStatus.ROOM, now);
            LobbyServerMessage createResultMessage = LobbyServerMessage.newBuilder()
                    .setTimestamp(ProtobufUtils.toTimestamp(now))
                    .setCreateRoomResult(ServerCreateRoomResult.newBuilder()
                            .setType(ServerCreateRoomResult.Type.SUCCESS)
                            .setRoomInfo(room.toRoomInfo())
                            .build())
                    .build();
            client.sendMessage(createResultMessage);
            handleServerUpdateRoomList(room, now);
        }

        private void handleEnterRoom(ClientEnterRoom enterRoom) {
            Long accountId = getAccountId();
            LocalDateTime now = LocalDateTime.now();
            log.debug("handleEnterRoom accountId={}, enterRoom={}", accountId, enterRoom);

            LobbyClient client = lobbyClientManager.getClient(accountId)
                    .orElseThrow();

            ServerEnterRoomResult.Type resultType;
            Room room = roomManager.getRoom(enterRoom.getRoomId()).orElse(null);
            if (room == null) {
                resultType = ServerEnterRoomResult.Type.NOT_FOUND;
            } else {
                resultType = room.enter(client) ? ServerEnterRoomResult.Type.SUCCESS : ServerEnterRoomResult.Type.ALREADY_FULL;
            }

            LobbyServerMessage serverMessage = LobbyServerMessage.newBuilder()
                    .setTimestamp(ProtobufUtils.toTimestamp(now))
                    .setEnterRoomResult(ServerEnterRoomResult.newBuilder()
                            .setType(resultType)
                            .build())
                    .build();
            client.sendMessage(serverMessage);
        }

        // 유저가 로비에 존재하는 방 목록이 필요한 경우
        private void handleServerRoomList(LobbyClient client, List<Room> rooms, LocalDateTime now) {
            LobbyServerMessage roomListMessage = LobbyServerMessage.newBuilder()
                    .setTimestamp(ProtobufUtils.toTimestamp(now))
                    .setRoomList(ServerRoomList.newBuilder()
                            .addAllRoomList(rooms.stream()
                                    .map(Room::toRoomInfo)
                                    .toList())
                            .build())
                    .build();
            client.sendMessage(roomListMessage);
        }

        // 특정 방에 유저가 들어가거나 나가서 변동이 발생한 경우 로비에 있는 유저에게 업데이트 된 정보를 뿌린다.
        private void handleServerUpdateRoomList(Room room, LocalDateTime now) {
            LobbyServerMessage updatedRoomMessage = LobbyServerMessage.newBuilder()
                    .setTimestamp(ProtobufUtils.toTimestamp(now))
                    .setUpdateRoomList(ServerUpdateRoomList.newBuilder()
                            .setUpdatedRoom(room.toRoomInfo())
                            .build())
                    .build();
            lobbyClientManager.broadcastLobby(updatedRoomMessage);
        }

        // 유저의 위치가 로비로 변경될 경우 로비에 존재하는 방 정보가 필요하므로 방 정보를 뿌린다.
        private void updateUserStatus(LobbyClient client, UserStatus userStatus, LocalDateTime now) {
            client.setUserStatus(userStatus);
            if (userStatus.equals(UserStatus.LOBBY)) {
                handleServerRoomList(client, roomManager.getRooms(), now);
            }
        }
    }
}
