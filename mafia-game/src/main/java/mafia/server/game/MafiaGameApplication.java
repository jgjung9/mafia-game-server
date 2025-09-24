package mafia.server.game;

import mafia.server.game.core.GameServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MafiaGameApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(MafiaGameApplication.class, args);
        GameServer gameServer = applicationContext.getBean(GameServer.class);

        gameServer.run();
    }
}
