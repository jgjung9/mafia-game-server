package mafia.server.data.domain.game.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("계정 아이디를 통해 유저를 찾을 수 있다")
    void findByAccountId() throws Exception {
        // given
        Long accountId = 1L;
        userRepository.save(createUser(accountId, "testnick"));

        // when
        User foundUser = userRepository.findByAccountId(accountId).get();

        // then
        assertThat(foundUser.getAccountId()).isEqualTo(accountId);
        assertThat(foundUser.getNickname()).isEqualTo("testnick");
    }

    @Test
    @DisplayName("닉네임을 통해 유저를 찾을 수 있다")
    void findByNickname() throws Exception {
        // given
        String nickname = "testnick";
        User user = createUser(1L, nickname);
        User savedUser = userRepository.save(user);

        // when
        User foundUser = userRepository.findByNickname(nickname).get();

        // then
        assertThat(foundUser).isEqualTo(savedUser);
    }

    private User createUser(Long accountId, String nickname) {
        return User.builder()
                .accountId(accountId)
                .nickname(nickname)
                .build();
    }
}