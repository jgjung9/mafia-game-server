package mafia.server.web.common.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public abstract class JwtUtils {

    public static final String HEADER_NAME = "Authorization";
    public static final String JWT_STARTS_WITH = "Bearer ";

    public static boolean hasToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER_NAME);
        if (!StringUtils.hasText(header)) {
            return false;
        }
        return header.startsWith(JWT_STARTS_WITH);
    }

    public static String getJwt(HttpServletRequest request) {
        return request.getHeader(HEADER_NAME).split(" ")[1];
    }
}
