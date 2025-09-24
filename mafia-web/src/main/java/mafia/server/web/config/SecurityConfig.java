package mafia.server.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/webapp/**", "/css/**", "/js/**", "/favicon.*").permitAll()
                        .requestMatchers("/rpc").authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
        ;
        return http.build();
    }
}
