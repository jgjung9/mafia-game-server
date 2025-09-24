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
    @DisplayName("닉네임을 통해 유저를 찾을 수 있다")
    void findByNickname() throws Exception {
        // given
        String nickname = "testnick";
        User user = createUser(nickname);
        User savedUser = userRepository.save(user);

        // when
        User foundUser = userRepository.findByNickname(nickname).get();

        // then
        assertThat(foundUser).isEqualTo(savedUser);
    }

    private User createUser(String nickname) {
        return User.builder()
                .accountId(1L)
                .nickname(nickname)
                .build();
    }
}