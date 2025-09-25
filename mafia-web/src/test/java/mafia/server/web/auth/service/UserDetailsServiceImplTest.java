package mafia.server.web.auth.service;

import mafia.server.data.domain.account.Account;
import mafia.server.data.domain.account.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UserDetailsServiceImplTest {

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    void tearDown() {
        accountRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("존재하는 계정 아이디를 요청시 정상 반환한다")
    void loadUserByUsername() throws Exception {
        // given
        String username = "testname1";
        accountRepository.save(createAccount(username));

        // when
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // then
        assertThat(userDetails.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("존재하지 않는 아이를 요청시 예외가 발생한다")
    void loadUserByUsername_shouldThrowExceptionIfNotExistsUsername() throws Exception {
        // given
        String username = "testname1";

        // when & then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Account not found: " + username);
    }

    private Account createAccount(String username) {
        return Account.builder()
                .username(username)
                .password("testpass123")
                .build();
    }
}