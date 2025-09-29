package mafia.server.lobby.grpc;

import io.grpc.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import mafia.server.lobby.common.Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class JwtInterceptor implements ServerInterceptor {

    private final JwtParser parser;

    public JwtInterceptor(@Value("${jwt.secret_key}") String jwtSecretKey) {
        this.parser = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)))
                .build();
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler
    ) {
        String authorizationHeader = metadata.get(Constant.AUTHORIZATION_METADATA_KEY);

        Status status = Status.OK;
        if (authorizationHeader == null) {
            status = Status.UNAUTHENTICATED.withDescription("Unauthenticated: Token is missing");
        } else if (!authorizationHeader.startsWith(Constant.BEARER_TYPE)) {
            status = Status.UNAUTHENTICATED.withDescription("Unauthenticated: unknown authorization type");
        } else {
            String token = authorizationHeader.substring(Constant.BEARER_TYPE.length()).trim();

            try {
                Claims claims = parser.parseSignedClaims(token).getPayload();
                Context ctx = Context.current().withValue(Constant.CLIENT_ID_CONTEXT_KEY, claims.getSubject());
                return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler);
            } catch (JwtException e) {
                status = Status.UNAUTHENTICATED.withDescription("Unauthenticated: invalid token")
                        .withCause(e);
            }
        }
        serverCall.close(status, new Metadata());
        return new ServerCall.Listener<ReqT>() {
        };
    }
}
