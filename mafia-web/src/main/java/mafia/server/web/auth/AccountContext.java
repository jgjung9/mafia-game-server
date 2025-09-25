package mafia.server.web.auth;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public record AccountContext(
        Long accountId,
        Collection<? extends GrantedAuthority> authorities
) {
}
