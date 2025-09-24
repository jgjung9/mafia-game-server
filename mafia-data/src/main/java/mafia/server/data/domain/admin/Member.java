package mafia.server.data.domain.admin;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mafia.server.data.domain.BaseTimeEntity;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", updatable = false)
    private Long id;

    private String username;
    private String password;

    @Builder
    private Member(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static Member create(String username, String password) {
        return Member.builder()
                .username(username)
                .password(password)
                .build();
    }
}
