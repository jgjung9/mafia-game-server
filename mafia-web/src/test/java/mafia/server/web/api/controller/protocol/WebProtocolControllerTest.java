package mafia.server.web.api.controller.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import mafia.server.web.WebProtocol.Request;
import mafia.server.web.api.command.CommandManager;
import mafia.server.web.api.command.UnknownCommand;
import mafia.server.web.auth.AccountContext;
import mafia.server.web.auth.filter.JwtAuthenticationFilter;
import mafia.server.web.auth.provider.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WebProtocolController.class)
class WebProtocolControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtProvider jwtProvider;
    @MockitoBean
    private CommandManager commandManager;

    @TestConfiguration
    static class TestSecurityConfiguration implements WebMvcConfigurer {

        @Autowired
        private ObjectMapper objectMapper;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/rpc").authenticated()
                    )
                    .addFilterBefore(new JwtAuthenticationFilter(jwtProvider(), objectMapper), UsernamePasswordAuthenticationFilter.class)
                    .csrf(AbstractHttpConfigurer::disable)
                    .formLogin(AbstractHttpConfigurer::disable)
                    .httpBasic(AbstractHttpConfigurer::disable)
            ;
            return http.build();
        }

        @Bean
        public JwtProvider jwtProvider() {
            return new JwtProvider(
                    "d606a53ae647793f00d9e72598d28457dc9c51ad1f84e33dcb74233531515b11",
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE
            );
        }
    }

    @Test
    @DisplayName("Web Protocol RPC 요청 시 유저는 인증되어야 한다")
    void rpcRequest_whenWithJwt() throws Exception {
        // given
        String accessToken = jwtProvider.generateToken(createAccountContext());
        Request request = Request.newBuilder()
                .setCommand(Request.Command.UNKNOWN)
                .build();
        given(commandManager.getCommand(any())).willReturn(new UnknownCommand());

        // when & then
        mockMvc.perform(post("/rpc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFormat.printer().print(request))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("FAILURE"))
        ;
    }

    @Test
    @DisplayName("jwt 토큰 없이 web protocol rpc 요청 시 접근이 거부된다")
    void rpcRequest_whenWithoutJwt() throws Exception {
        // given
        Request request = Request.newBuilder()
                .setCommand(Request.Command.UNKNOWN)
                .build();
        given(commandManager.getCommand(any())).willReturn(new UnknownCommand());

        // when & then
        mockMvc.perform(post("/rpc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonFormat.printer().print(request))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
        ;
    }

    private AccountContext createAccountContext() {
        return new AccountContext(1L, List.of());
    }
}