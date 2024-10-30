package fiji.plugin.trackmate.detector.lacss;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import fiji.plugin.trackmate.detector.lacss.LacssGrpc.LacssBlockingStub;
import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.TlsChannelCredentials;

public class LacssClient {
    private final int MAX_MESSAGE_SIZE = 1024*1024*512 ;
    private final String host;
    private final String token;
    private final Process localProcess;
    private final String modelPath;

    public Status status;

    public String getModelPath() {
        return modelPath;
    }

    public LacssClient(String host, String token) {
        if (token != null && token.trim().length() == 0) {
            token = null;
        }
        this.token = token;
        this.host = host;
        this.localProcess = null;
        this.modelPath = null;
        this.status = Status.OK;
    }

    public LacssClient(String modelPath) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("python", "-m", "lacss.deploy.remote_server", "--local", modelPath);
        pb.inheritIO().redirectErrorStream();
        this.localProcess = pb.start();

        this.token = null;
        this.host = null;

        this.modelPath = modelPath;
    }

    public void shutdownLocalProcess() {
        if (localProcess != null && localProcess.isAlive()) {
            localProcess.destroy();
            try {
                localProcess.waitFor();
            } catch(InterruptedException e) {}
        }
    }

    private int guess_port() {
        int port = 443 ;

        if (host.indexOf(':') > -1) {
            String[] arr = host.split(":");

            port = Integer.parseInt(arr[1]);

        }

        return port ;
    }

    ManagedChannel getChannel() {
        ChannelCredentials cred ;

        if ( guess_port() == 443 ) {
            cred = TlsChannelCredentials.create() ;
        } else {
            cred = InsecureChannelCredentials.create() ;
        }

        ManagedChannel channel = Grpc.newChannelBuilder(host, cred)
            .build();

        return channel;
    }

    public LacssMsg.Results runDetection(LacssMsg.Input inputs) throws InterruptedException {
        LacssMsg.Results results = null;
        ManagedChannel channel = null;

        try {
            channel = getChannel() ; 
            LacssBlockingStub stub = LacssGrpc.newBlockingStub(channel)
                .withWaitForReady()
                .withCompression("gzip")
                .withMaxOutboundMessageSize(MAX_MESSAGE_SIZE)
                .withMaxInboundMessageSize(MAX_MESSAGE_SIZE)
                .withDeadlineAfter(180, TimeUnit.SECONDS);

            if (token != null) {
                LacssTokenCredentials callCredentials = new LacssTokenCredentials(token);
                stub = stub.withCallCredentials(callCredentials);
            }

            results = stub.runDetection(inputs);

        } catch (StatusRuntimeException e) {
            status = Status.fromThrowable(e);
            results = null;
        }
        finally{
            if (channel != null) {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            }
        }
 
        return results;
    }

    @Override
    public String toString() {
        if (modelPath != null) {
            return "Local: " + getModelPath();
        } else {
            return "Remote: " + host + ":" + token;
        }
    }
}
