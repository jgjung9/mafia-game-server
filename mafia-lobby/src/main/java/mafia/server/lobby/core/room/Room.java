package mafia.server.lobby.core.room;

import lombok.Getter;
import lombok.ToString;
import mafia.server.lobby.core.LobbyClient;
import mafia.server.lobby.protocol.LobbyServerMessage;

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

    public Room(int id, String title, LobbyClient creator) {
        this.id = id;
        this.title = title;
        this.hostId = creator.getAccountId();
        members.put(creator.getAccountId(), creator);
    }

    public void enterUser(LobbyClient user) {
        members.put(user.getAccountId(), user);

        // TODO: 서버 유저가 입장했음을 알린다.
        LobbyServerMessage serverMessage = LobbyServerMessage.newBuilder()
                .build();
        broadcast(serverMessage);
    }

    public void leaveUser(Long accountId) {
        LobbyClient removed = members.remove(accountId);

        // TODO: 유저가 떠났음을 알린다.
        LobbyServerMessage serverMessage = LobbyServerMessage.newBuilder()
                .build();
        broadcast(serverMessage);

        // TODO: 떠난 유저가 방장이면 다른 유저로 방장이 변경된다
    }

    public void startGame() {
        // TODO: 게임을 시작한다
    }

    public int getUserCount() {
        return members.size();
    }

    private void broadcast(LobbyServerMessage serverMessage) {
        members.values()
                .forEach(client -> client.sendMessage(serverMessage));
    }

}
