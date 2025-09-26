package mafia.server.web.api.command.user;

import mafia.server.data.domain.game.user.User;
import mafia.server.data.domain.game.user.UserRepository;
import mafia.server.web.WebProtocol;
import mafia.server.web.WebProtocol.Response.Result;
import mafia.server.web.WebProtocol.UserCreateRes.UserCreateResult;
import mafia.server.web.auth.AccountContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static mafia.server.web.WebProtocol.Request.Command.USER_CREATE;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserCreateCommandTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserCreateCommand userCreateCommand;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("유저 생성에 성공한다")
    void execute_resultSuccess() throws Exception {
        // given
        long accountId = 1L;
        String nickname = "testnick";
        LocalDateTime now = LocalDateTime.now();
        AccountContext accountContext = createAccountContext(accountId);
        WebProtocol.Request request = createRequest(nickname);

        // when
        WebProtocol.Response response = userCreateCommand.execute(accountContext, request, now);

        // then
        WebProtocol.UserCreateRes userCreateRes = response.getUserCreateRes();
        assertThat(response.getCommand()).isEqualTo(USER_CREATE);
        assertThat(response.getResult()).isEqualTo(Result.SUCCESS);
        assertThat(userCreateRes.getUserCreateResult()).isEqualTo(UserCreateResult.SUCCESS);
    }

    @Test
    @DisplayName("유저가 존재하는 경우 생성할 수 없다")
    void execute_resultExistsUser() throws Exception {
        // given
        long accountId = 1L;
        String nickname = "testnick";
        userRepository.save(createUser(accountId, nickname));

        LocalDateTime now = LocalDateTime.now();
        AccountContext accountContext = createAccountContext(accountId);
        WebProtocol.Request request = createRequest("테스트닉123");

        // when
        WebProtocol.Response response = userCreateCommand.execute(accountContext, request, now);

        // then
        WebProtocol.UserCreateRes userCreateRes = response.getUserCreateRes();
        assertThat(response.getCommand()).isEqualTo(USER_CREATE);
        assertThat(response.getResult()).isEqualTo(Result.SUCCESS);
        assertThat(userCreateRes.getUserCreateResult()).isEqualTo(UserCreateResult.EXISTS_USER);
    }

    @Test
    @DisplayName("이미 존재하는 닉네임으로 유저를 생성할 수 없다")
    void execute_resultExistsNickname() throws Exception {
        String nickname = "testnick";
        userRepository.save(createUser(1L, nickname));

        LocalDateTime now = LocalDateTime.now();
        AccountContext accountContext = createAccountContext(2L);
        WebProtocol.Request request = createRequest(nickname);

        // when
        WebProtocol.Response response = userCreateCommand.execute(accountContext, request, now);

        // then
        WebProtocol.UserCreateRes userCreateRes = response.getUserCreateRes();
        assertThat(response.getCommand()).isEqualTo(USER_CREATE);
        assertThat(response.getResult()).isEqualTo(Result.SUCCESS);
        assertThat(userCreateRes.getUserCreateResult()).isEqualTo(UserCreateResult.EXISTS_NICKNAME);
    }

    @Test
    @DisplayName("유효하지 않은 닉네임 형태로 유저를 생성할 수 없다")
    void execute_resultInvalidNickname() throws Exception {
        // given
        long accountId = 1L;
        String nickname = "longlonglongnickname1234";
        LocalDateTime now = LocalDateTime.now();
        AccountContext accountContext = createAccountContext(accountId);
        WebProtocol.Request request = createRequest(nickname);

        // when
        WebProtocol.Response response = userCreateCommand.execute(accountContext, request, now);

        // then
        WebProtocol.UserCreateRes userCreateRes = response.getUserCreateRes();
        assertThat(response.getCommand()).isEqualTo(USER_CREATE);
        assertThat(response.getResult()).isEqualTo(Result.SUCCESS);
        assertThat(userCreateRes.getUserCreateResult()).isEqualTo(UserCreateResult.INVALID_NICKNAME);
    }

    private WebProtocol.Request createRequest(String nickname) {
        return WebProtocol.Request.newBuilder()
                .setCommand(userCreateCommand.getCommandType())
                .setUserCreateReq(WebProtocol.UserCreateReq.newBuilder()
                        .setNickname(nickname)
                        .build())
                .build();
    }

    public User createUser(Long accountId, String nickname) {
        return User.builder()
                .accountId(accountId)
                .nickname(nickname)
                .build();
    }

    public AccountContext createAccountContext(Long accountId) {
        return new AccountContext(accountId, List.of());
    }

}