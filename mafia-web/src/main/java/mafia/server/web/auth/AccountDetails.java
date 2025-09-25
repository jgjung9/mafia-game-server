package mafia.server.web.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AccountDetails implements UserDetails {

    @Getter
    private final AccountDto accountDto;
    private final List<GrantedAuthority> authorities;

    public AccountDetails(AccountDto accountDto, List<GrantedAuthority> authorities) {
        this.accountDto = accountDto;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return accountDto.password();
    }

    @Override
    public String getUsername() {
        return accountDto.username();
    }

}
