package mafia.server.data.domain.account;

import mafia.server.data.common.Const;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {


    @Test
    @DisplayName("계정을 생성할 수 있다")
    void createAccount() throws Exception {
        // given
        String username = "testname";
        String password = "testpass1";

        // when
        Account account = Account.create(username, password);

        // then
        assertThat(account.getUsername()).isEqualTo(username);
        assertThat(account.getPassword()).isEqualTo(password);
    }

    @Test
    @DisplayName("계정의 ID는 최소 길이 이상이어야 한다")
    void shouldBeUsernameLengthGreaterThanEqual() throws Exception {
        // given
        StringBuilder sb = new StringBuilder();
        int minLength = Const.ACCOUNT_USERNAME_MIN_LENGTH;
        for (int i = 0; i < minLength - 1; i++) {
            sb.append("a");
        }
        String username = sb.toString();

        // when & then
        assertThatThrownBy(() -> Account.create(username, "testpass1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게정의 ID 길이는 " + Const.ACCOUNT_USERNAME_MIN_LENGTH + "~" + Const.ACCOUNT_USERNAME_MAX_LENGTH + " 여야 합니다");
    }

    @Test
    @DisplayName("계정의 ID는 최대 길이 이하여야 한다")
    void shouldBeUsernameLengthLessThanEqual() throws Exception {
        // given
        StringBuilder sb = new StringBuilder();
        int maxLength = Const.ACCOUNT_USERNAME_MAX_LENGTH;
        for (int i = 0; i < maxLength + 1; i++) {
            sb.append("a");
        }
        String username = sb.toString();

        // when & then
        assertThatThrownBy(() -> Account.create(username, "testpass1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게정의 ID 길이는 " + Const.ACCOUNT_USERNAME_MIN_LENGTH + "~" + Const.ACCOUNT_USERNAME_MAX_LENGTH + " 여야 합니다");
    }
}