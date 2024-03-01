package fiji.plugin.trackmate.detector.lacss;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import fiji.plugin.trackmate.detector.lacss.LacssGrpc.LacssBlockingStub;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;

public class LacssClient {
    private final ManagedChannel channel;
    private final LacssBlockingStub blockingStub;
    private final String token;
    private final Process localProcess;
    private final String modelPath;

    public String getModelPath() {
        return modelPath;
    }

    LacssClient(String host, String token) {
        if (token != null && token.trim().length() == 0) {
            token = null;
        }
        this.token = token;
        
        this.channel = Grpc.newChannelBuilder(host, InsecureChannelCredentials.create())
            .build();
        this.blockingStub = LacssGrpc.newBlockingStub(channel);
        
        this.localProcess = null;
        this.modelPath = null;
    }

    LacssClient(String modelPath) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("python", "-m", "lacss.deploy.remote_server", "--local", modelPath);
        pb.inheritIO().redirectErrorStream();
        this.localProcess = pb.start();

        String target = "localhost:50051";
        this.channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
            .build();
        this.blockingStub = LacssGrpc.newBlockingStub(channel)
            .withWaitForReady();

        this.token = null;
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

    public LacssMsg.PolygonResult runDetection(LacssMsg.Input inputs) {
        LacssBlockingStub stub = blockingStub.withDeadlineAfter(90, TimeUnit.SECONDS);

        if (token != null) {
            LacssTokenCredentials callCredentials = new LacssTokenCredentials(token);
            stub = stub.withCallCredentials(callCredentials);
        }

        return stub.runDetection(inputs);
    }

    @Override
    public void finalize() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {}
    }

    @Override
    public String toString() {
        if (modelPath != null) {
            return "Local: " + getModelPath();
        } else {
            return "Remote: " + blockingStub.getChannel() + ":" + token;
        }
    }
}
