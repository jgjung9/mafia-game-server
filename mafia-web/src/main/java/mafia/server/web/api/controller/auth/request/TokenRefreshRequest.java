package mafia.server.web.api.controller.auth.request;

public record TokenRefreshRequest(
        String refreshToken
) {
}
