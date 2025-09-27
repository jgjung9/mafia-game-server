package mafia.server.web.service.game;

import lombok.RequiredArgsConstructor;
import mafia.server.data.domain.game.user.User;
import mafia.server.data.domain.game.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findByAccountId(Long accountId) {
        return userRepository.findByAccountId(accountId)
                .orElse(null);
    }
}
