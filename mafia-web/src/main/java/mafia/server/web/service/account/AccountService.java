package mafia.server.web.service.account;

import lombok.RequiredArgsConstructor;
import mafia.server.data.domain.account.Account;
import mafia.server.data.domain.account.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {

    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;

    @Transactional
    public Long createAccount(String username, String password) {
        boolean exists = accountRepository.existsByUsername(username);
        if (exists) {
            throw new IllegalArgumentException("이미 존재하는 유저 ID 입니다");
        }
        return accountRepository.save(Account.create(username, passwordEncoder.encode(password)))
                .getId();
    }
}
