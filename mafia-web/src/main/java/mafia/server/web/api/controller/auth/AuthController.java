package mafia.server.web.api.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mafia.server.web.api.controller.ApiResponse;
import mafia.server.web.api.controller.auth.request.SignupRequest;
import mafia.server.web.common.annotation.ExecutionTime;
import mafia.server.web.service.account.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ExecutionTime
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AccountService accountService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        accountService.createAccount(request.username(), request.password());
        return ResponseEntity.created(null)
                .body(ApiResponse.created());
    }
}
