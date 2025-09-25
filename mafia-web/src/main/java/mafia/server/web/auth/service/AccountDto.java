package mafia.server.web.auth.service;

import mafia.server.data.domain.account.Account;

public record AccountDto(
        Long id, String username, String password
) {

    public static AccountDto from(Account account) {
        return new AccountDto(account.getId(), account.getUsername(), account.getPassword());
    }
}
