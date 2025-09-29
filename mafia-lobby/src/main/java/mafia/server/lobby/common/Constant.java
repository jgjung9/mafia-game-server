package mafia.server.lobby.common;

import io.grpc.Context;
import io.grpc.Metadata;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public abstract class Constant {

    public static final String BEARER_TYPE = "Bearer";
    public static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization", ASCII_STRING_MARSHALLER);
    public static final Context.Key<String> CLIENT_ID_CONTEXT_KEY = Context.key("accountId");
}
