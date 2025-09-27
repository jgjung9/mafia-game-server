package mafia.server.web.api.command.user;

import mafia.server.data.domain.game.user.User;
import mafia.server.data.domain.game.user.UserRepository;
import mafia.server.web.WebProtocol;
import mafia.server.web.WebProtocol.Request.Command;
import mafia.server.web.WebProtocol.UserGetInfoRes.UserGetInfoResult;
import mafia.server.web.auth.AccountContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserGetInfoCommandTest {

    @Autowired
    private UserGetInfoCommand userGetInfoCommand;
    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("유저를 정보를 반환한다")
    void execute_returnUserInfo() throws Exception {
        // given
        Long accountId = 1L;
        String nickname = "testnick";
        int level = 10;
        long exp = 2000;
        userRepository.save(createUser(accountId, nickname, level, exp));
        AccountContext accountContext = createAccountContext(accountId);
        WebProtocol.Request request = createRequest();
        LocalDateTime now = LocalDateTime.now();

        // when
        WebProtocol.Response response = userGetInfoCommand.execute(accountContext, request, now);

        // then
        WebProtocol.UserGetInfoRes userGetInfoRes = response.getUserGetInfoRes();
        assertThat(response.getCommand()).isEqualTo(Command.USER_GET_INFO);
        assertThat(userGetInfoRes.getUserGetInfoResult()).isEqualTo(UserGetInfoResult.SUCCESS);
        assertThat(userGetInfoRes.getNickname()).isEqualTo(nickname);
        assertThat(userGetInfoRes.getLevel()).isEqualTo(level);
        assertThat(userGetInfoRes.getExp()).isEqualTo(exp);
    }

    @Test
    @DisplayName("계정 아이디에 해당하는 유저가 없을 경우 결과로 유저가 존재하지 않음을 반환한다")
    void execute_returnUserNotFoundWhenNotExists() throws Exception {
        // given
        Long accountId = 1L;
        AccountContext accountContext = createAccountContext(accountId);
        WebProtocol.Request request = createRequest();
        LocalDateTime now = LocalDateTime.now();

        // when
        WebProtocol.Response response = userGetInfoCommand.execute(accountContext, request, now);

        // then
        WebProtocol.UserGetInfoRes userGetInfoRes = response.getUserGetInfoRes();
        assertThat(response.getCommand()).isEqualTo(Command.USER_GET_INFO);
        assertThat(userGetInfoRes.getUserGetInfoResult()).isEqualTo(UserGetInfoResult.USER_NOT_FOUND);
    }

    private User createUser(Long accountId, String nickname, int level, long exp) {
        return User.builder()
                .accountId(accountId)
                .nickname(nickname)
                .level(level)
                .exp(exp)
                .build();
    }

    private AccountContext createAccountContext(Long accountId) {
        return new AccountContext(accountId, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private WebProtocol.Request createRequest() {
        return WebProtocol.Request.newBuilder()
                .setCommand(Command.USER_GET_INFO)
                .build();
    }

}