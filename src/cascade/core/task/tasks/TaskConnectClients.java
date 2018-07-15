package cascade.core.task.tasks;

import cascade.core.CascadeServer;
import cascade.core.LogLevel;
import cascade.core.backend.ServerBackend;
import cascade.core.task.IServerTask;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskConnectClients implements IServerTask {

    // TODO: Server configuration, 10 is the max clients that are able to connect and wait at the auth prompt.
    private final ExecutorService connectionExecutor = Executors.newFixedThreadPool(10);
    private final ServerBackend backend;
    private final ServerSocket server;

    public TaskConnectClients(ServerBackend backend, ServerSocket socket) {
        this.server = socket;
        this.backend = backend;
    }

    @Override
    public void execute() {
        Thread.currentThread().setName("Job#ConnectClients");

        try {
            Socket socket = server.accept();

            // execute the authenticating process on another thread.
            connectionExecutor.execute(() -> backend.getConnectionHandler().connectNewClient(socket));

        } catch (IOException exception) {
            CascadeServer.log("Failed to connect new client.", LogLevel.INFO);
        }

    }

    @Override
    public void shutdown() {
        connectionExecutor.shutdownNow();
    }
}
