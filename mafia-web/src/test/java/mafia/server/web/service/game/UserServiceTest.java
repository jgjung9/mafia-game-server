package mafia.server.web.service.game;

import mafia.server.data.domain.game.user.User;
import mafia.server.data.domain.game.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("유저가를 계정 아이디로 찾을 수 있다")
    void findByAccountId() throws Exception {
        // given
        Long accountId = 1L;
        String nickname = "testnick";
        userRepository.save(createUser(accountId, nickname));

        // when
        User foundUser = userService.findByAccountId(accountId);

        // then
        assertThat(foundUser.getAccountId()).isEqualTo(accountId);
        assertThat(foundUser.getNickname()).isEqualTo(nickname);
    }

    @Test
    @DisplayName("계정 아이디에 해당하는 유저가 존재하지 않으면 null을 반환한다")
    void findByAccountId_returnNullWhenNotExists() throws Exception {
        // given
        Long accountId = 1L;

        // when &then
        assertThat(userService.findByAccountId(accountId)).isNull();
    }

    private User createUser(Long accountId, String nickname) {
        return User.builder()
                .accountId(accountId)
                .nickname(nickname)
                .build();
    }
}