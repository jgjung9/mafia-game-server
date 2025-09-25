package mafia.server.web.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mafia.server.web.auth.AccountContext;
import mafia.server.web.auth.provider.JwtProvider;
import mafia.server.web.common.jwt.JwtUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!JwtUtils.hasToken(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = JwtUtils.getJwt(request);
        if (!jwtProvider.validateToken(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }
        AccountContext accountContext = jwtProvider.getAccountContextFromToken(jwt);
        log.debug("JwtAuthenticationFilter accountContext={}", accountContext);
        SecurityContextHolder.getContextHolderStrategy().getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(accountContext, null, accountContext.authorities()));
        filterChain.doFilter(request, response);
    }
}
