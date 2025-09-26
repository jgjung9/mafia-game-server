package mafia.server.web.auth.service;

import lombok.RequiredArgsConstructor;
import mafia.server.data.domain.account.Account;
import mafia.server.data.domain.account.AccountRepository;
import mafia.server.web.auth.AccountDetails;
import mafia.server.web.auth.AccountDto;
import mafia.server.web.common.exception.ErrorCode;
import mafia.server.web.common.exception.MafiaServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found: " + username));

        switch (account.getStatus()) {
            case ACTIVE -> {
            }
            case DELETED -> throw new MafiaServiceException("관리자에 의해 삭제된 계정입니다", ErrorCode.ACCOUNT_DELETED);
            case SUSPEND -> throw new MafiaServiceException("정지된 계정입니다", ErrorCode.ACCOUNT_SUSPEND);
            case WITHDRAWN -> throw new MafiaServiceException("탈퇴된 계정입니다", ErrorCode.ACCOUNT_WITHDRAWN);
            case DORMANT -> throw new MafiaServiceException("휴면 계정입나다", ErrorCode.ACCOUNT_DORMANT);
            default -> throw new MafiaServiceException("존재하지 않는 계정 상태입니다", ErrorCode.ACCOUNT_INVALID_STATUS);
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new AccountDetails(AccountDto.from(account), authorities);
    }
}
