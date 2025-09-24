package mafia.server.data.domain.account;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

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

    @Test
    @DisplayName("존재하는 유저 아이디 여부를 확인한다")
    void existsByUsername() throws Exception {
        // given
        String username = "testname";
        String password = "1234";
        accountRepository.save(createAccount(username, password));

        // when
        boolean found = accountRepository.existsByUsername(username);
        boolean notFound = accountRepository.existsByUsername("ffanguo12f");

        // then
        assertThat(found).isTrue();
        assertThat(notFound).isFalse();
    }

    private Account createAccount(String username, String password) {
        return Account.builder()
                .username(username)
                .password(password)
                .build();
    }
}