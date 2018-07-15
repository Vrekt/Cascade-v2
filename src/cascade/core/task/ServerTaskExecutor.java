package cascade.core.task;

import cascade.core.CascadeServer;
import cascade.core.LogLevel;
import cascade.core.backend.ServerBackend;
import cascade.core.task.tasks.TaskConnectClients;
import cascade.core.task.tasks.TaskVerifyClients;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerTaskExecutor {

    private final ExecutorService jobExecutor = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    private final ServerBackend backend;

    private TaskConnectClients taskConnectClients;
    private TaskVerifyClients taskVerifyClients;

    public ServerTaskExecutor(ServerBackend backend) {
        this.backend = backend;
    }

    /**
     * Start all threads.
     */
    public void start(ServerSocket socket) {

        taskConnectClients = new TaskConnectClients(backend, socket);
        taskVerifyClients = new TaskVerifyClients(backend);

        jobExecutor.execute(() -> {
            while (backend.isRunning()) {
                taskConnectClients.execute();
            }
        });

        scheduledExecutor.scheduleWithFixedDelay(() -> {
            taskVerifyClients.execute();
        }, 1000, 1000, TimeUnit.MILLISECONDS);

        CascadeServer.log("Server has been started!", LogLevel.INFO);

    }

    /**
     * Stop the executorservice.
     */
    public void stop() {
        jobExecutor.shutdownNow();
        scheduledExecutor.shutdownNow();

        // end tasks.
        taskConnectClients.shutdown();
        taskVerifyClients.shutdown();

    }

}
