package fiji.plugin.trackmate.detector.lacss;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

import java.util.concurrent.Executor;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;

public class LacssTokenCredentials extends CallCredentials {

    private final String token;
    static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization",
            ASCII_STRING_MARSHALLER);

    LacssTokenCredentials(String token) {
        this.token = token;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
        appExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Metadata headers = new Metadata();
                    headers.put(
                            AUTHORIZATION_METADATA_KEY,
                            String.format("Bearer %s", token));
                    applier.apply(headers);
                } catch (Throwable e) {
                    applier.fail(Status.UNAUTHENTICATED.withCause(e));
                }
            }
        });
    }

    @Override
    public void thisUsesUnstableApi() {
    }

}
