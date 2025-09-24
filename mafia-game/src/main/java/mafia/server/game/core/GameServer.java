package mafia.server.game.core;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mafia.server.data.domain.game.User;
import mafia.server.data.domain.game.UserRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameServer {

    private final UserRepository userRepository;

    public void run() {
        User savedUser = userRepository.save(User.create("test", 1));
        log.info("savedUser={}", savedUser);
    }

    @PreDestroy
    public void shutdown() {

    }
}
