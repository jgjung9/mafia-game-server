package mafia.server.web.api.controller.auth.request;

import org.hibernate.validator.constraints.Length;

public record LoginRequest(
        @Length(min = 8, max = 16) String username,
        @Length(min = 8, max = 16) String password
) {
}
