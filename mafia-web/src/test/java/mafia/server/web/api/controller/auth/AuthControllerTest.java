package mafia.server.web.api.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import mafia.server.web.api.controller.auth.request.LoginRequest;
import mafia.server.web.api.controller.auth.request.SignupRequest;
import mafia.server.web.auth.AccountDetails;
import mafia.server.web.auth.AccountDto;
import mafia.server.web.auth.provider.JwtProvider;
import mafia.server.web.service.account.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private JwtProvider jwtProvider;

    @TestConfiguration
    static class TestSecurityConfiguration {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()
                    )
                    .csrf(AbstractHttpConfigurer::disable)
            ;
            return http.build();
        }
    }

    @Test
    @DisplayName("계정 생성에 성공한다")
    void signup() throws Exception {
        // given
        SignupRequest request = new SignupRequest("test1234", "password123");

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                        .accept("application/json")
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("201"))
        ;
    }

    @Test
    @DisplayName("계정의 아이디 값이 8자리 미만일 경우 실패한다")
    void signup_shouldBeUsernameLengthGreaterThanEqual8() throws Exception {
        // given
        SignupRequest request = new SignupRequest("shortid", "password123");

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                        .accept("application/json")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"))
        ;
    }

    @Test
    @DisplayName("계정의 아이디 값이 16자리 초과할 경우 실패한다")
    void signup_shouldBeUsernameLengthLessThanEqual16() throws Exception {
        // given
        SignupRequest request = new SignupRequest("toomanylonglengthid1234", "password123");

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                        .accept("application/json")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"))
        ;
    }

    @Test
    @DisplayName("계정의 비밀번호 값이 8자리 미만일 경우 실패한다")
    void signup_shouldBePasswordLengthGreaterThanEqual8() throws Exception {
        // given
        SignupRequest request = new SignupRequest("test1234", "short1");

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                        .accept("application/json")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"))
        ;
    }

    @Test
    @DisplayName("계정의 비밀번호 값이 16자리 초과할 경우 실패한다")
    void signup_shouldBePasswordLengthLessThanEqual16() throws Exception {
        // given
        SignupRequest request = new SignupRequest("test1234", "toolonglongpassword123");

        // when & then
        mockMvc.perform(post("/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                        .accept("application/json")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"))
        ;
    }

    @Test
    @DisplayName("정상적으로 로그인에 성공한다")
    void login() throws Exception {
        // given
        given(authenticationManager.authenticate(any())).willReturn(createTestingAuthentication());
        given(jwtProvider.generateToken(any())).willReturn("testToken");
        given(jwtProvider.generateRefreshToken(any())).willReturn("testToken");
        given(jwtProvider.getTokenType()).willReturn("Bearer");
        given(jwtProvider.getExpirationTime()).willReturn(3600L);
        LoginRequest request = new LoginRequest("test1234", "password123");

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.accessToken").value("testToken"))
                .andExpect(jsonPath("$.body.refreshToken").value("testToken"))
                .andExpect(jsonPath("$.body.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.body.expiresIn").value(3600))
        ;
    }

    @Test
    @DisplayName("로그인 요청 아이디 길이는 8자리 이상이어야 한다")
    void login_shouldBeLoginUsernameLengthGreaterThanEqual8() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test12", "password123");

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"))
        ;
    }

    @Test
    @DisplayName("로그인 요청 아이디 길이는 16자리 이하여야 한다")
    void login_shouldBeLoginUsernameLengthLessThanEqual16() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test1234longlonglong", "password123");

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"))
        ;
    }

    @Test
    @DisplayName("로그인 요청 비밀번호 길이는 8자리 이상이어야 한다")
    void login_shouldBeLoginPasswordLengthGreaterThanEqual8() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test1234", "short1");

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"))
        ;
    }

    @Test
    @DisplayName("로그인 요청 비밀번호 길이는 16자리 이하여야 한다")
    void login_shouldBeLoginPasswordLengthLessThanEqual16() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test1234", "password123longlonglong");

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"))
        ;
    }

    private Authentication createTestingAuthentication() {
        AccountDto accountDto = new AccountDto(1L, "test1234", "password123");
        AccountDetails accountDetails = new AccountDetails(accountDto, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        return new TestingAuthenticationToken(accountDetails, null, accountDetails.getAuthorities());
    }

}