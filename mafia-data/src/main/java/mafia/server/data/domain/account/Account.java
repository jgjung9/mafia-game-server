package mafia.server.data.domain.account;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Builder
    private Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static Account create(String username, String password) {
        return Account.builder()
                .username(username)
                .password(password)
                .build();
    }
}
