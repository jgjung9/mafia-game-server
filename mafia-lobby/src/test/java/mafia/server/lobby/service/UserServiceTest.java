package mafia.server.lobby.service;

import mafia.server.data.domain.game.user.User;
import mafia.server.data.domain.game.user.UserRepository;
import mafia.server.lobby.core.UserDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("계정 아이디를 통해 유저정보를 찾을 수 있다")
    void findByAccountId() throws Exception {
        // given
        Long accountId = 1L;
        String nickname = "testnick";

        // when
        User savedUser = userRepository.save(createUser(accountId, nickname));

        // then
        UserDto userDto = userService.findByAccountId(accountId);
        assertThat(userDto.userId()).isEqualTo(savedUser.getId());
        assertThat(userDto.nickname()).isEqualTo(nickname);
    }

    @Test
    @DisplayName("계정의 아이디에 해당하는 유저가 없으면 예외가 반환된다")
    void findByAccount_whenNotExists() throws Exception {
        // given
        Long accountId = 1L;

        // when & then
        assertThatThrownBy(() -> userService.findByAccountId(accountId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not Found: accountId=" + accountId);
    }

    private User createUser(Long accountId, String nickname) {
        return User.builder()
                .accountId(accountId)
                .nickname(nickname)
                .level(1)
                .exp(0)
                .build();
    }
}