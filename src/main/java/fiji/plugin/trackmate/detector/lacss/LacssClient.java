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
        this.token = token;
        this.channel = Grpc.newChannelBuilder(host, InsecureChannelCredentials.create()).build();
        this.blockingStub = LacssGrpc.newBlockingStub(channel);
        this.localProcess = null;
        this.modelPath = null;
    }

    LacssClient(String modelPath) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("python", "-m", "lacss.deploy.remote_server", "--local", modelPath);
        pb.inheritIO().redirectErrorStream();
        localProcess = pb.start();

        String target = "localhost:50051";
        channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
            .build();
        blockingStub = LacssGrpc.newBlockingStub(channel)
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
        if (token != null) {
            LacssTokenCredentials callCredentials = new LacssTokenCredentials(token);
            return blockingStub
                    .withCallCredentials(callCredentials)
                    .runDetection(inputs);
        } else {
            return blockingStub.runDetection(inputs);
        }
    }

    @Override
    public void finalize() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {}
    }
}
