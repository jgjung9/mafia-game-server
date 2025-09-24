package mafia.server.data.domain.game;

import jakarta.persistence.*;
import lombok.*;
import mafia.server.data.domain.BaseTimeEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String nickname;
    private int age;

    @Builder
    private User(String nickname, int age) {
        this.nickname = nickname;
        this.age = age;
    }

    public static User create(String nickname, int age) {
        return User.builder()
                .nickname(nickname)
                .age(age)
                .build();
    }
}
