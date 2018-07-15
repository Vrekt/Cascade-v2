package cascade.core;

import cascade.core.backend.ServerBackend;
import protocol.connection.AuthenticationType;

import java.io.IOException;
import java.net.ServerSocket;

public class CascadeServer {

    private static ServerBackend backend;

    /**
     * Start the server.
     *
     * @param port     the port
     * @param type     the auth type.
     * @param password the password.
     */
    public static void start(int port, AuthenticationType type, String password) {

        backend = new ServerBackend(type, password);
        log("Attempting to bind to port: " + port + ".", LogLevel.INFO);
        ServerSocket socket;
        try {
            socket = new ServerSocket(port);
            backend.start(socket);
        } catch (IOException exception) {
            log("Failed to bind to port: " + port + ".", LogLevel.ERROR);
            System.exit(0);
        }

    }

    /**
     * Start the server.
     *
     * @param port the port
     * @param type the auth type.
     */
    public static void start(int port, AuthenticationType type) {

        backend = new ServerBackend(type);
        log("Attempting to bind to port: " + port + ".", LogLevel.INFO);
        ServerSocket socket;
        try {
            socket = new ServerSocket(port);
            backend.start(socket);
        } catch (IOException exception) {
            log("Failed to bind to port: " + port + ".", LogLevel.ERROR);
            System.exit(0);
        }

    }

    public static void stop() {
        backend.stop();
    }

    /**
     * Log a message to the console.
     *
     * @param infos the information to log.
     * @param level the level.
     */
    public static void log(String infos, LogLevel level) {
        System.out.println("[" + Thread.currentThread().getName() + "/" + level.toString() + "]: " + infos);
    }

    public static ServerBackend getBackend() {
        return backend;
    }
}
