package mafia.server.lobby.core.room;

import lombok.Getter;
import lombok.ToString;
import mafia.server.lobby.common.ProtobufUtils;
import mafia.server.lobby.core.LobbyClient;
import mafia.server.lobby.protocol.Common;
import mafia.server.lobby.protocol.LobbyServerMessage;
import mafia.server.lobby.protocol.ServerUpdateRoom;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ToString
public class Room {

    @Getter
    private final int id;
    @Getter
    private Long hostId;
    @Getter
    private String title;
    private final RoomManager roomManager;
    private final Map<Long, LobbyClient> members = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> readyMap = new ConcurrentHashMap<>();
    private final int DEFAULT_MAX_USER_COUNT = 8;

    public Room(int id, String title, LobbyClient creator, RoomManager roomManager) {
        this.id = id;
        this.title = title;
        this.hostId = creator.getAccountId();
        this.roomManager = roomManager;
        members.put(creator.getAccountId(), creator);
        readyMap.put(creator.getAccountId(), false);
        roomManager.registerUser(creator.getAccountId(), id);
    }

    public synchronized boolean enter(LobbyClient client) {
        if (isFull()) {
            return false;
        }

        members.put(client.getAccountId(), client);
        readyMap.put(client.getAccountId(), false);
        roomManager.registerUser(client.getAccountId(), id);

        // 방에 있는 사람들에게 입장을 알린다.
        LobbyServerMessage serverMessage = LobbyServerMessage.newBuilder()
                .setTimestamp(ProtobufUtils.toTimestamp(LocalDateTime.now()))
                .setUpdateRoom(ServerUpdateRoom.newBuilder()
                        .setType(ServerUpdateRoom.Type.ENTER)
                        .setAccountId(client.getAccountId())
                        .build())
                .build();
        broadcast(serverMessage);
        return true;
    }

    public synchronized void leave(Long accountId) {
        LobbyClient removed = members.remove(accountId);
        readyMap.remove(accountId);
        roomManager.removeUser(removed.getAccountId());

        // 유저가 떠났음을 알린다.
        LobbyServerMessage serverMessage = LobbyServerMessage.newBuilder()
                .setTimestamp(ProtobufUtils.toTimestamp(LocalDateTime.now()))
                .setUpdateRoom(ServerUpdateRoom.newBuilder()
                        .setType(ServerUpdateRoom.Type.LEAVE)
                        .setAccountId(removed.getAccountId())
                        .build())
                .build();
        broadcast(serverMessage);

        // 유저의 수가 0이 될 경우 방을 삭제한다.
        if (getUserCount() == 0) {
            roomManager.removeRoom(getId());
        }

        // 떠난 유저가 방장이면 다른 유저로 방장이 변경된다
        if (removed.getAccountId().equals(hostId)) {
            changeHost();
        }
    }

    public void startGame() {
        // TODO: 게임을 시작한다
    }

    public void broadcast(LobbyServerMessage serverMessage) {
        members.values()
                .forEach(client -> client.sendMessage(serverMessage));
    }

    public void changeUserReady(Long accountId) {
        boolean isReady = !readyMap.get(accountId);
        readyMap.put(accountId, isReady);

        LobbyServerMessage serverMessage = LobbyServerMessage.newBuilder()
                .setTimestamp(ProtobufUtils.toTimestamp(LocalDateTime.now()))
                .setUpdateRoom(ServerUpdateRoom.newBuilder()
                        .setType(isReady ? ServerUpdateRoom.Type.READY : ServerUpdateRoom.Type.NOT_READY)
                        .setAccountId(accountId)
                        .build())
                .build();
    }

    private void changeHost() {
        LobbyClient randomClient = members.values().stream().findAny()
                .orElseThrow();
        hostId = randomClient.getAccountId();
        LobbyServerMessage serverMessage = LobbyServerMessage.newBuilder()
                .setTimestamp(ProtobufUtils.toTimestamp(LocalDateTime.now()))
                .setUpdateRoom(ServerUpdateRoom.newBuilder()
                        .setType(ServerUpdateRoom.Type.CHANGE_HOST)
                        .setAccountId(randomClient.getAccountId())
                        .build())
                .build();
        broadcast(serverMessage);
    }

    public int getUserCount() {
        return members.size();
    }

    public boolean isHost(Long accountId) {
        return hostId.equals(accountId);
    }

    public boolean isFull() {
        return getUserCount() == DEFAULT_MAX_USER_COUNT;
    }

    public Common.RoomInfo toRoomInfo() {
        return Common.RoomInfo.newBuilder()
                .setRoomId(id)
                .setTitle(title)
                .setUserCount(getUserCount())
                .build();
    }

    public List<Common.RoomUser> getRoomUsers() {
        return members.values().stream()
                .map(client -> Common.RoomUser.newBuilder()
                        .setAccountId(client.getAccountId())
                        .setHost(isHost(client.getAccountId()))
                        .setReady(readyMap.get(client.getAccountId()))
                        .build())
                .toList();
    }
}
