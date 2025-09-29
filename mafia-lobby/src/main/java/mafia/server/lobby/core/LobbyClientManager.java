package mafia.server.lobby.core;

import lombok.extern.slf4j.Slf4j;
import mafia.server.lobby.protocol.LobbyServerMessage;
import mafia.server.lobby.protocol.UserStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class LobbyClientManager {

    private final Map<Long, LobbyClient> clients = new ConcurrentHashMap<>();

    public void addClient(Long accountId, LobbyClient lobbyClient) {
        clients.put(accountId, lobbyClient);
    }

    public synchronized void removeClient(Long accountId) {
        log.info("Client Disconnect: accountId={}", accountId);
        LobbyClient removed = clients.remove(accountId);
        removed.close();
    }

    public Optional<LobbyClient> getClient(Long accountId) {
        return Optional.ofNullable(clients.get(accountId));
    }

    public void broadcastLobby(LobbyServerMessage serverMessage) {
        clients.values().stream()
                .filter(client -> client.getUserStatus().equals(UserStatus.LOBBY))
                .forEach(client -> client.sendMessage(serverMessage));
    }

    public void sendMessage(Long accountId, LobbyServerMessage serverMessage) {
        getClient(accountId).ifPresent(client -> client.sendMessage(serverMessage));
    }

    public synchronized void removeAll() {
        for (Long accountId : clients.keySet()) {
            LobbyClient removed = clients.remove(accountId);
            removed.close();
        }
    }
}
