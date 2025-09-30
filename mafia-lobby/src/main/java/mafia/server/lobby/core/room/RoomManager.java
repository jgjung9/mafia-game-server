package mafia.server.lobby.core.room;

import lombok.extern.slf4j.Slf4j;
import mafia.server.lobby.core.LobbyClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RoomManager {

    private AtomicInteger sequence = new AtomicInteger(0);

    private Map<Integer, Room> rooms = new ConcurrentHashMap<>();
    private Map<Long, Integer> userRoomMap = new ConcurrentHashMap<>(); // key: accountId, value: roomId

    public Room createRoom(String title, LobbyClient creator) {
        int roomId = sequence.incrementAndGet();
        Room room = new Room(roomId, title, creator, this);
        rooms.put(roomId, room);
        log.debug("Room Created: {}", room);
        return room;
    }

    public void removeRoom(Integer roomId) {
        rooms.remove(roomId);
    }

    public void removeUser(Long accountId) {
        userRoomMap.remove(accountId);
    }

    public Optional<Room> getRoomByAccountId(Long accountId) {
        Integer roomId = userRoomMap.getOrDefault(accountId, 0);
        return roomId == 0 ? Optional.empty() : getRoom(roomId);
    }

    public Optional<Room> getRoom(Integer roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    public List<Room> getRooms() {
        return rooms.values().stream().toList();
    }

    public void registerUser(Long accountId, int roomId) {
        userRoomMap.put(accountId, roomId);
    }
}
