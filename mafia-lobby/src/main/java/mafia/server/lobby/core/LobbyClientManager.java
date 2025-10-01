package mafia.server.lobby.core;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import mafia.server.lobby.protocol.LobbyServerMessage;
import mafia.server.lobby.protocol.UserStatus;
import org.springframework.stereotype.Component;

import java.util.List;
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
        log.info("LobbyClientManager remove client: accountId={}", accountId);
        clients.remove(accountId);
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

    public void removeAll() {
        List.copyOf(clients.keySet()).forEach(accountId -> getClient(accountId)
                .ifPresent(LobbyClient::close));
    }

    @PreDestroy
    public void destroy() {
        removeAll();
    }
}
