package mafia.server.web.auth.provider;

import mafia.server.web.auth.AccountContext;
import mafia.server.web.auth.AccountDetails;
import mafia.server.web.auth.AccountDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    private final String jwtSecretKey = "d606a53ae647793f00d9e72598d28457dc9c51ad1f84e33dcb74233531515b11";
    private final long expirationTime = 1L;
    private final long refreshExpirationTime = 1L;
    private JwtProvider jwtProvider = new JwtProvider(jwtSecretKey, expirationTime, refreshExpirationTime);

    @Test
    @DisplayName("토큰을 생성한다")
    void generateToken() throws Exception {
        // given
        AccountDetails accountDetails = createAccountDetails(1L, "testname1");

        // when
        String token = jwtProvider.generateToken(accountDetails);
        String refreshToken = jwtProvider.generateRefreshToken(accountDetails);

        // then
        assertThat(token).isNotEmpty();
        assertThat(token).hasSizeGreaterThan(128);

        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshToken).hasSizeGreaterThan(128);
    }

    @Test
    @DisplayName("AccountContext 를 이용해 토큰을 생성한다")
    void generateTokenByAccountContext() throws Exception {
        // given
        AccountContext accountContext = createAccountContext(1L);

        // when
        String token = jwtProvider.generateToken(accountContext);
        String refreshToken = jwtProvider.generateRefreshToken(accountContext);

        // then
        assertThat(token).isNotEmpty();
        assertThat(token).hasSizeGreaterThan(128);

        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshToken).hasSizeGreaterThan(128);
    }

    @Test
    @DisplayName("생성된 토큰을 검증할 수 있다")
    void validateToken() throws Exception {
        // given
        AccountDetails accountDetails = createAccountDetails(1L, "testname1");
        String token = jwtProvider.generateToken(accountDetails);
        String refreshToken = jwtProvider.generateRefreshToken(accountDetails);

        // when & then
        assertThat(jwtProvider.validateToken(token)).isTrue();
        assertThat(jwtProvider.validateToken(refreshToken)).isTrue();
    }

    @Test
    @DisplayName("만료된 토큰은 검증할 수 있다")
    void validateToken_tokenWhenExpired() throws Exception {
        // given
        AccountDetails accountDetails = createAccountDetails(1L, "testname1");
        String token = jwtProvider.generateToken(accountDetails);
        String refreshToken = jwtProvider.generateRefreshToken(accountDetails);

        // when
        Thread.sleep(expirationTime * 1000);

        // then
        assertThat(jwtProvider.validateToken(token)).isFalse();
        assertThat(jwtProvider.validateToken(refreshToken)).isFalse();
    }

    @Test
    @DisplayName("유효하지 않은 토큰을 검증할 수 있다")
    void validateToken_whenInvalidValue() throws Exception {
        // given
        String token = "invalidvalidfioewfsdfdfsf";

        // when & then
        assertThat(jwtProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("토큰을 통해 계정 정보를 가져올 수 있다")
    void getAccountContextFromToken() throws Exception {
        // given
        AccountDetails accountDetails = createAccountDetails(1L, "testname1");
        String token = jwtProvider.generateToken(accountDetails);

        // when
        AccountContext accountContext = jwtProvider.getAccountContextFromToken(token);

        // then
        assertThat(accountContext.accountId()).isEqualTo(1L);
        assertThat(accountContext.authorities()).isEqualTo(accountDetails.getAuthorities());
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 계정정보를 가지고 올 수 없다")
    void getAccountContextFromToken_whenInvalidToken() throws Exception {
        // given
        String token = "invalidvalidfioewfsdfdfsf";

        // when & then
        assertThatThrownBy(() -> jwtProvider.getAccountContextFromToken(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid Token: " + token);
    }

    private AccountDetails createAccountDetails(Long accountId, String username) {
        AccountDto accountDto = new AccountDto(accountId, username, "password1");
        return new AccountDetails(accountDto, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private AccountContext createAccountContext(Long accountId) {
        return new AccountContext(accountId, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}