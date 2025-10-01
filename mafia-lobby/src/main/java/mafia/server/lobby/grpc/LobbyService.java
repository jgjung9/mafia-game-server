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
                case CHAT_ROOM -> handleChatRoom(lobbyClientMessage.getChatRoom());
                case LEAVE_ROOM -> handleLeaveRoom(lobbyClientMessage.getLeaveRoom());
                case INVITE_ROOM -> handleInviteRoom(lobbyClientMessage.getInviteRoom());
                case REPLY_INVITE_ROOM -> handleReplyInviteRoom(lobbyClientMessage.getReplyInviteRoom());
                case READY_ROOM -> handleReadyRoom(lobbyClientMessage.getReadyRoom());
            }
        }

        @Override
        public void onError(Throwable throwable) {
            log.info("Grpc LobbyService Error: {}", throwable.getMessage(), throwable);
            Long accountId = getAccountId();
            LobbyClient client = getClient(accountId);
            handleDisconnect(client, ServerDisconnect.Type.SERVER);
            client.close();
        }

        @Override
        public void onCompleted() {
            getClient(getAccountId()).close();
        }

        private Long getAccountId() {
            return Long.valueOf(Constant.CLIENT_ID_CONTEXT_KEY.get());
        }

        // 클라 요청 처리
        private void handleConnect(ClientConnect connect) {
            Long accountId = getAccountId();
            LocalDateTime now = LocalDateTime.now();
            log.debug("handleConnect accountId={}", accountId);

            UserDto userDto = userService.findByAccountId(accountId);
            lobbyClientManager.getClient(accountId)
                    .ifPresentOrElse(client -> {
                        handleDisconnect(client, ServerDisconnect.Type.OVERLAP);
                        // HACK: 이렇게 써도 되는지 확인 필요
                        onCompleted();
                    }, () -> lobbyClientManager.addClient(accountId, new LobbyClient(accountId, userDto, responseObserver, lobbyClientManager, roomManager)));
        }

        private void handleSetUserStatus(ClientSetUserStatus setUserStatus) {
            Long accountId = getAccountId();
            LocalDateTime now = LocalDateTime.now();
            log.debug("handleSetUserStatus accountId={}, setUserStatus={}", accountId, setUserStatus);

            LobbyClient client = getClient(accountId);
            updateUserStatus(client, setUserStatus.getUserStatus(), now);
        }

        private void handleChatAll(ClientChatAll chatAll) {
            Long accountId = getAccountId();
            LocalDateTime now = LocalDateTime.now();
            log.debug("handleChatAll accountId={}, chatAll={}", accountId, chatAll);

            UserDto userDto = getClient(accountId).getUserDto();

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

            UserDto userDto = getClient(accountId).getUserDto();

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

            LobbyClient client = getClient(accountId);
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

            LobbyClient client = getClient(accountId);
            ServerEnterRoomResult.Type resultType;
            Room room = roomManager.getRoom(enterRoom.getRoomId()).orElse(null);
            if (room == null) {
                resultType = ServerEnterRoomResult.Type.NOT_FOUND;
            } else {
                resultType = room.enter(client) ? ServerEnterRoomResult.Type.SUCCESS : ServerEnterRoomResult.Type.ALREADY_FULL;
            }

            ServerEnterRoomResult.Builder resultBuilder = ServerEnterRoomResult.newBuilder()
                    .setType(resultType);
            if (resultType.equals(ServerEnterRoomResult.Type.SUCCESS)) {
                resultBuilder.setRoomDetail(Common.RoomDetail.newBuilder()
                        .setRoomInfo(room.toRoomInfo())
                        .addAllUsers(room.getRoomUsers())
                        .build());
            }
            LobbyServerMessage serverMessage = LobbyServerMessage.newBuilder()
                    .setTimestamp(ProtobufUtils.toTimestamp(now))
                    .setEnterRoomResult(resultBuilder.build())
                    .build();
            client.sendMessage(serverMessage);
        }

        private void handleChatRoom(ClientChatRoom chatRoom) {
            Long accountId = getAccountId();
            LocalDateTime now = LocalDateTime.now();
            log.debug("handleChatRoom accountId={}, chatRoom={}", accountId, chatRoom);

            UserDto userDto = getClient(accountId).getUserDto();
            Room room = roomManager.getRoomByAccountId(accountId)
                    .orElseThrow();

            LobbyServerMessage serverMessage = LobbyServerMessage.newBuilder()
                    .setTimestamp(ProtobufUtils.toTimestamp(now))
                    .setChatRoom(ServerChatRoom.newBuilder()
                            .setAccountId(accountId)
                            .setNickname(userDto.nickname())
                            .setMessage(chatRoom.getMessage())
                            .build())
                    .build();
            room.broadcast(serverMessage);
        }

        private void handleLeaveRoom(ClientLeaveRoom leaveRoom) {
            Long accountId = getAccountId();
            LocalDateTime now = LocalDateTime.now();
            log.debug("handleLeaveRoom accountId={}, leaveRoom={}", accountId, leaveRoom);

            LobbyClient client = getClient(accountId);
            Room room = roomManager.getRoomByAccountId(accountId)
                    .orElseThrow();
            room.leave(accountId);
            updateUserStatus(client, UserStatus.LOBBY, now);
        }

        private void handleInviteRoom(ClientInviteRoom inviteRoom) {
            Long accountId = getAccountId();
            LocalDateTime now = LocalDateTime.now();
            log.debug("handleInviteRoom accountId={}, inviteRoom={}", accountId, inviteRoom);

            LobbyClient senderClient = getClient(accountId);
            Room room = roomManager.getRoomByAccountId(accountId).orElseThrow();
            if (!room.isHost(accountId)) {
                return;
            }

            lobbyClientManager.getClient(inviteRoom.getAccountId())
                    .ifPresent(receiverClient -> {
                        if (!receiverClient.getUserStatus().equals(UserStatus.LOBBY)) {
                            return;
                        }
                        LobbyServerMessage serverMessage = LobbyServerMessage.newBuilder()
                                .setTimestamp(ProtobufUtils.toTimestamp(now))
                                .setInviteRoom(ServerInviteRoom.newBuilder()
                                        .setAccountId(accountId)
                                        .setNickname(senderClient.getUserDto().nickname())
                                        .setRoomId(room.getId())
                                        .build())
                                .build();
                        receiverClient.sendMessage(serverMessage);
                    });
        }

        private void handleReplyInviteRoom(ClientReplyInviteRoom replyInviteRoom) {
            Long accountId = getAccountId();
            LocalDateTime now = LocalDateTime.now();
            log.debug("handleReplyInviteRoom accountId={}, replyInviteRoom={}", accountId, replyInviteRoom);

            LobbyClient client = getClient(accountId);
            Room room = roomManager.getRoom(replyInviteRoom.getRoomId()).orElse(null);

            ServerReplyInviteRoomResult.Type resultType = ServerReplyInviteRoomResult.Type.SUCCESS;
            if (room == null) {
                resultType = ServerReplyInviteRoomResult.Type.EXPIRED;
            } else if (!room.enter(client)) {
                resultType = ServerReplyInviteRoomResult.Type.ALREADY_FULL;
            }

            LobbyServerMessage serverMessage = LobbyServerMessage.newBuilder()
                    .setTimestamp(ProtobufUtils.toTimestamp(now))
                    .setReplyInviteRoomResult(ServerReplyInviteRoomResult.newBuilder()
                            .setType(resultType)
                            .build())
                    .build();
            client.sendMessage(serverMessage);
        }

        private void handleReadyRoom(ClientReadyRoom readyRoom) {
            Long accountId = getAccountId();
            LocalDateTime now = LocalDateTime.now();
            log.debug("handleReadyRoom accountId={}, readyRoom={}", accountId, readyRoom);

            Room room = roomManager.getRoomByAccountId(accountId).orElseThrow();
            room.changeUserReady(accountId);
        }

        // 상태 변화에 따라 서버 측에서 먼저 보내는 응답

        private void handleDisconnect(LobbyClient client, ServerDisconnect.Type type) {
            LobbyServerMessage disconnectMessage = LobbyServerMessage.newBuilder()
                    .setTimestamp(ProtobufUtils.toTimestamp(LocalDateTime.now()))
                    .setDisconnect(ServerDisconnect.newBuilder()
                            .setType(type)
                            .build())
                    .build();
            client.sendMessage(disconnectMessage);
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

        private LobbyClient getClient(Long accountId) {
            return lobbyClientManager.getClient(accountId)
                    .orElseThrow();
        }
    }
}
