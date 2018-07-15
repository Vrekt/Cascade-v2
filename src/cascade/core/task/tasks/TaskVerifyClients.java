package cascade.core.task.tasks;

import cascade.core.backend.ServerBackend;
import cascade.core.task.IServerTask;

public class TaskVerifyClients implements IServerTask {

    private final ServerBackend backend;

    public TaskVerifyClients(ServerBackend backend) {
        this.backend = backend;
    }

    @Override
    public void execute() {
        Thread.currentThread().setName("Job#VerifyClients");
        backend.verify();
    }

    @Override
    public void shutdown() {
        // nothing to do.
    }

}
