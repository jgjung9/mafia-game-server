package mafia.server.game.core;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameServer {

    public void run() {
        
    }

    @PreDestroy
    public void shutdown() {

    }
}
