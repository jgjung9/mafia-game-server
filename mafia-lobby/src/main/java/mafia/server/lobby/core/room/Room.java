package mafia.server.lobby.core.room;

import lombok.Getter;
import lombok.ToString;
import mafia.server.lobby.common.ProtobufUtils;
import mafia.server.lobby.core.LobbyClient;
import mafia.server.lobby.protocol.Common;
import mafia.server.lobby.protocol.LobbyServerMessage;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ToString
public class Room {

    @Getter
    private final int id;
    @Getter
    private final Long hostId;
    private final Map<Long, LobbyClient> members = new ConcurrentHashMap<>();
    @Getter
    private String title;
    private final RoomManager roomManager;
    private final int DEFAULT_MAX_USER_COUNT = 8;

    public Room(int id, String title, LobbyClient creator, RoomManager roomManager) {
        this.id = id;
        this.title = title;
        this.hostId = creator.getAccountId();
        this.roomManager = roomManager;
        members.put(creator.getAccountId(), creator);
    }

    public synchronized boolean enter(LobbyClient client) {
        if (getUserCount() >= DEFAULT_MAX_USER_COUNT) {
            return false;
        }
        members.put(client.getAccountId(), client);
        // TODO: 방에 있는 사람들에게 입장을 알린다.
        LobbyServerMessage serverMessage = LobbyServerMessage.newBuilder()
                .setTimestamp(ProtobufUtils.toTimestamp(LocalDateTime.now()))
                .build();
        broadcast(serverMessage);
        return true;
    }

    public synchronized void leave(Long accountId) {
        LobbyClient removed = members.remove(accountId);

        // TODO: 유저가 떠났음을 알린다.
        LobbyServerMessage serverMessage = LobbyServerMessage.newBuilder()
                .setTimestamp(ProtobufUtils.toTimestamp(LocalDateTime.now()))
                .build();
        broadcast(serverMessage);

        // 유저의 수가 0이 될 경우 방을 삭제한다.
        if (getUserCount() == 0) {
            roomManager.removeRoom(getId());
            return;
        }

        // TODO: 떠난 유저가 방장이면 다른 유저로 방장이 변경된다

    }

    public void startGame() {
        // TODO: 게임을 시작한다
    }

    public int getUserCount() {
        return members.size();
    }

    public void broadcast(LobbyServerMessage serverMessage) {
        members.values()
                .forEach(client -> client.sendMessage(serverMessage));
    }

    public Common.RoomInfo toRoomInfo() {
        return Common.RoomInfo.newBuilder()
                .setRoomId(id)
                .setTitle(title)
                .setUserCount(getUserCount())
                .build();
    }
}
