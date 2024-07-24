package fiji.plugin.trackmate.detector.lacss;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import fiji.plugin.trackmate.detector.lacss.LacssGrpc.LacssBlockingStub;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Status;

public class LacssClient {
    private final String host;
    private final String token;
    private final Process localProcess;
    private final String modelPath;

    public Status status;

    public String getModelPath() {
        return modelPath;
    }

    LacssClient(String host, String token) {
        if (token != null && token.trim().length() == 0) {
            token = null;
        }
        this.token = token;
        this.host = host;
        this.localProcess = null;
        this.modelPath = null;
        this.status = Status.OK;
    }

    LacssClient(String modelPath) throws IOException {
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

    public LacssMsg.PolygonResult runDetection(LacssMsg.Input inputs) throws InterruptedException {
        LacssMsg.PolygonResult results = null;
        ManagedChannel channel = null;

        try {
            channel = Grpc.newChannelBuilder(host, InsecureChannelCredentials.create())
                .build();
            LacssBlockingStub stub = LacssGrpc.newBlockingStub(channel)
                .withWaitForReady()
                .withDeadlineAfter(180, TimeUnit.SECONDS);            

            if (token != null) {
                LacssTokenCredentials callCredentials = new LacssTokenCredentials(token);
                stub = stub.withCallCredentials(callCredentials);
            }

            results = stub.runDetection(inputs);

        } catch (Exception e) {
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
