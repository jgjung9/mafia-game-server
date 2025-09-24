package mafia.server.data.domain.game;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

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
                .nickname(nickname)
                .age(1)
                .build();
    }
}