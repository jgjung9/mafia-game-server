package mafia.server.data.domain.game.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mafia.server.data.common.Const;
import mafia.server.data.domain.BaseTimeEntity;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private Long accountId;
    private String nickname;
    private String description;
    private int level;
    private long exp;
    private long diamond;
    private LocalDateTime lastLoginAt;

    @Builder
    public User(Long accountId, String nickname, String description, int level, long exp, long diamond, LocalDateTime lastLoginAt) {
        this.accountId = accountId;
        this.nickname = nickname;
        this.description = description;
        this.level = level;
        this.exp = exp;
        this.diamond = diamond;
        this.lastLoginAt = lastLoginAt;
    }

    public static User create(Long accountId, String nickname) {
        if (nickname.length() < Const.NICKNAME_MIN_LENGTH || nickname.length() > Const.NICKNAME_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "닉네임 길이는 " + Const.NICKNAME_MIN_LENGTH + "~" + Const.NICKNAME_MAX_LENGTH + " 여야 합니다"
            );
        }

        return User.builder()
                .accountId(accountId)
                .nickname(nickname)
                .level(1)
                .diamond(0)
                .exp(0)
                .build();
    }
}
