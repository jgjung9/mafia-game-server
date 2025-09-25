package mafia.server.web.api.exception;

public class JwtRefreshException extends RuntimeException {

    public JwtRefreshException(String message) {
        super(message);
    }

    public JwtRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
