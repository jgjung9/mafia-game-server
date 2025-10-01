package mafia.server.lobby.service;

import lombok.RequiredArgsConstructor;
import mafia.server.data.domain.game.user.UserRepository;
import mafia.server.lobby.core.UserDto;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto findByAccountId(Long accountId) {
        return userRepository.findByAccountId(accountId)
                .map(UserDto::from)
                .orElseThrow(() -> new IllegalArgumentException("User not Found: accountId=" + accountId));
    }
}
