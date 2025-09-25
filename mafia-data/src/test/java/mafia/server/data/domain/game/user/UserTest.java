package mafia.server.data.domain.game.user;

import mafia.server.data.common.Const;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    @DisplayName("유저를 생성한다")
    void createUser() throws Exception {
        // given
        String nickname = "testnick";

        // when
        User user = User.create(1L, nickname);

        // then
        assertThat(user.getAccountId()).isEqualTo(1L);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getLevel()).isEqualTo(1);
        assertThat(user.getExp()).isEqualTo(0);
    }

    @Test
    @DisplayName("닉네임 글자수가 최소치 미만일 경우 유저 생성에 실패한다")
    void shouldBeNicknameLengthGreaterThanEqual() throws Exception {
        // given
        int minLength = Const.NICKNAME_MIN_LENGTH;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < minLength - 1; i++) {
            sb.append("a");
        }
        String nickname = sb.toString();

        // when & then
        assertThatThrownBy(() -> User.create(1L, nickname))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임 길이는 " + Const.NICKNAME_MIN_LENGTH + "~" + Const.NICKNAME_MAX_LENGTH + " 여야 합니다");
    }

    @Test
    @DisplayName("닉네임 글자수는 최대치 글자 이하여야 한다")
    void shouldBeNicknameLengthLessThenEqual() throws Exception {
        // given
        int maxLength = Const.NICKNAME_MAX_LENGTH;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxLength + 1; i++) {
            sb.append("a");
        }
        String nickname = sb.toString();

        // when & then
        assertThatThrownBy(() -> User.create(1L, nickname))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임 길이는 " + Const.NICKNAME_MIN_LENGTH + "~" + Const.NICKNAME_MAX_LENGTH + " 여야 합니다");
    }
}