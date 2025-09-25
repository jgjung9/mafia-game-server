package mafia.server.web.api.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mafia.server.data.infra.redis.service.TokenRedisService;
import mafia.server.web.api.controller.ApiResponse;
import mafia.server.web.api.controller.auth.request.LoginRequest;
import mafia.server.web.api.controller.auth.request.SignupRequest;
import mafia.server.web.api.controller.auth.response.LoginResponse;
import mafia.server.web.auth.AccountDetails;
import mafia.server.web.auth.provider.JwtProvider;
import mafia.server.web.common.annotation.ExecutionTime;
import mafia.server.web.service.account.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@ExecutionTime
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final TokenRedisService tokenRedisService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        accountService.createAccount(request.username(), request.password());
        return ResponseEntity.created(null)
                .body(ApiResponse.created());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        AccountDetails accountDetails = (AccountDetails) authenticate.getPrincipal();
        String refreshToken = jwtProvider.generateRefreshToken(accountDetails);
        tokenRedisService.save(accountDetails.getAccountDto().id(), refreshToken, jwtProvider.getRefreshExpirationTime());
        LoginResponse response = new LoginResponse(
                jwtProvider.generateToken(accountDetails), refreshToken,
                jwtProvider.getTokenType(), jwtProvider.getExpirationTime()
        );
        log.debug("loginResponse={}", response);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
