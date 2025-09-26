package mafia.server.web.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mafia.server.web.api.controller.ApiResponse;
import mafia.server.web.auth.AccountContext;
import mafia.server.web.auth.provider.JwtProvider;
import mafia.server.web.common.jwt.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!JwtUtils.hasToken(request)) {
            sendUnauthorized(request, response);
            return;
        }

        String jwt = JwtUtils.getJwt(request);
        if (!jwtProvider.validateToken(jwt)) {
            sendUnauthorized(request, response);
            return;
        }

        AccountContext accountContext = jwtProvider.getAccountContextFromToken(jwt);
        log.debug("JwtAuthenticationFilter accountContext={}", accountContext);
        SecurityContextHolder.getContextHolderStrategy().getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(accountContext, null, accountContext.authorities()));
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // request 추후 요청 측 정보 로깅이 필요하면 사용
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter()
                .write(objectMapper.writeValueAsString(
                        ApiResponse.unauthorized(null, "유효하지 않은 토큰입니다. 재로그인 하거나, 토큰을 리프레시 하세요")
                ));
    }
}
