package mafia.server.data.domain.account;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountStatus {
    ACTIVE("정상"),
    DELETED("삭제"),
    SUSPEND("정지"),
    WITHDRAWN("탈퇴"),
    DORMANT("휴면");

    private final String description;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
