package mafia.server.web.service.account;

import mafia.server.data.domain.account.Account;
import mafia.server.data.domain.account.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AccountServiceTest {

    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        accountRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("계정을 생성한다")
    void createAccount() throws Exception {
        // given
        String username = "testid123";
        String password = "testpass1234";

        // when
        Long savedId = accountService.createAccount(username, password);

        // then
        Account foundAccount = accountRepository.findById(savedId).get();
        assertThat(foundAccount.getId()).isEqualTo(savedId);
        assertThat(foundAccount.getUsername()).isEqualTo(username);
        assertThat(passwordEncoder.matches(password, foundAccount.getPassword())).isTrue();
    }

    @Test
    @DisplayName("이미 존재하는 아이디로 계정 생성 요청 시 실패한다")
    void createAccount_shouldThrowExceptionIfAlreadyExistsUsername() throws Exception {
        // given
        String username = "testid123";
        Account account = createAccount(username, "testpass123");
        accountRepository.save(account);

        // when & then
        assertThatThrownBy(() -> accountService.createAccount(username, "testpass123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 유저 ID 입니다");
    }

    private Account createAccount(String username, String password) {
        return Account.builder()
                .username(username)
                .password(password)
                .build();
    }
}