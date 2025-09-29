package mafia.server.lobby.core;

import mafia.server.data.domain.game.user.User;

public record UserDto(
        Long userId, String nickname
) {
    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getNickname());
    }
}
