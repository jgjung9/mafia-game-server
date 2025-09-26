package mafia.server.data.domain.account;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mafia.server.data.common.Const;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id", updatable = false)
    private Long id;

    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    private LocalDateTime lastLoginAt;

    @Builder
    private Account(String username, String password, AccountStatus status, LocalDateTime lastLoginAt) {
        this.username = username;
        this.password = password;
        this.status = status;
        this.lastLoginAt = lastLoginAt;
    }

    public static Account create(String username, String password) {
        if (!StringUtils.hasText(username)
                || username.length() < Const.ACCOUNT_USERNAME_MIN_LENGTH
                || username.length() > Const.ACCOUNT_USERNAME_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "게정의 ID 길이는 " + Const.ACCOUNT_USERNAME_MIN_LENGTH + "~" + Const.ACCOUNT_USERNAME_MAX_LENGTH + " 여야 합니다"
            );
        }

        return Account.builder()
                .username(username)
                .password(password)
                .status(AccountStatus.ACTIVE)
                .build();
    }
}
