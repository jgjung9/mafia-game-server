package mafia.server.data.domain.account;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private Environment environment;

    @AfterEach
    void tearDown() {
        accountRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("유저ID로 유저를 찾을 수 있다.")
    void findByUsername() throws Exception {
        // given
        String username = "testname";
        String password = "1234";
        accountRepository.save(createAccount(username, password));

        // when
        Account foundAccount = accountRepository.findByUsername(username).get();

        // then
        assertThat(foundAccount.getUsername()).isEqualTo(username);
        assertThat(foundAccount.getPassword()).isEqualTo(password);
    }

    private Account createAccount(String username, String password) {
        return Account.builder()
                .username(username)
                .password(password)
                .build();
    }
}