package cascade.core.task;

import cascade.core.backend.ServerBackend;

public interface IServerTask {

    /**
     * Execute the task.
     *
     * @param backend the backend.
     */
    void execute();

    void shutdown();

}
