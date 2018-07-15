package cascade.core.backend;

import cascade.core.CascadeServer;
import cascade.core.LogLevel;
import cascade.core.authentication.ConnectionHandler;
import cascade.core.client.Client;
import cascade.core.client.session.Session;
import cascade.core.task.ServerTaskExecutor;
import protocol.Packet;
import protocol.connection.AuthenticationType;
import protocol.connection.ConnectionStatus;
import protocol.packets.PacketConnectionStatus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerBackend {

    private Map<Integer, Client> clientMap = new ConcurrentHashMap<>();
    private Map<Socket, Long> authenticationTimeoutMap = new ConcurrentHashMap<>();

    private String password;

    private boolean running = true;
    private final ConnectionHandler connectionHandler;

    private final ServerTaskExecutor executor = new ServerTaskExecutor(this);

    public ServerBackend(AuthenticationType type) {
        connectionHandler = new ConnectionHandler(type);
    }

    public ServerBackend(AuthenticationType type, String serverPassword) {
        this.password = serverPassword;
        connectionHandler = new ConnectionHandler(type);
    }

    /**
     * Start the server,
     */
    public void start(ServerSocket socket) {
        CascadeServer.log("Starting server...", LogLevel.INFO);
        executor.start(socket);
    }

    /**
     * Stop the server.
     */
    public void stop() {
        running = false;
        CascadeServer.log("Stopping server...", LogLevel.INFO);
        executor.stop();
    }

    /**
     * @param verify the string to check.
     * @return true, if the string matches the password.
     */
    public boolean checkPassword(String verify) {
        return verify.equals(password);
    }

    /**
     * After the client has been verified it will be given a session and put into the map.
     *
     * @param username their username.
     * @param socket   the socket connection
     * @param in       the data stream
     * @param out      the data stream
     */
    public void clientAuthenticated(String username, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        try {
            out.writeObject(new PacketConnectionStatus(ConnectionStatus.CONNECTED));
        } catch (IOException exception) {
            CascadeServer.log("Could not connect new client after authenticating.", LogLevel.WARN);
            // return
            return;
        }

        int uniqueId = clientMap.size();
        authenticationTimeoutMap.remove(socket);

        CascadeServer.log("Connected new client with the username: " + username + " and an ID of: " + uniqueId, LogLevel.INFO);
        Client client = new Client(new Session(username, uniqueId), socket, in, out);
        client.setLastKeepAlive(System.currentTimeMillis());
        client.start();

        clientMap.put(uniqueId, client);
    }

    /**
     * Once a client connects they are put into a map to be timed out after 10 seconds.
     *
     * @param socket the socket connection.
     */
    public void clientAwaitingAuthentication(Socket socket) {
        authenticationTimeoutMap.put(socket, System.currentTimeMillis());
    }

    /**
     * Called if a client gets kicked.
     *
     * @param client the client.
     */
    public void clientDisconnected(Client client) {
        CascadeServer.log(client.getSession().getUsername() + " has disconnected.", LogLevel.INFO);

        client.closeConnection();
        clientMap.remove(client.getSession().getUniqueId(), client);
    }

    /**
     * @param username the clients username.
     * @return true, if the username is already taken.
     */
    public boolean isUsernameTaken(String username) {
        return clientMap.values().stream().anyMatch(client -> client.getSession().getUsername().equals(username));
    }

    /**
     * Send a packet to all connected clients.
     *
     * @param packet the packet.
     */
    public void sendPacketToAllClients(Packet packet) {
        clientMap.values().forEach(client -> client.write(packet));
    }

    /**
     * Verify all clients are still connected.
     */
    public void verify() {
        for (Socket socket : authenticationTimeoutMap.keySet()) {
            long time = System.currentTimeMillis() - authenticationTimeoutMap.get(socket);
            if (time >= 10000) {
                // took too long to authenticate.
                try {
                    socket.close();
                    authenticationTimeoutMap.remove(socket);
                } catch (IOException exception) {
                    // do nothing.
                }
            }
        }

        for (Client client : clientMap.values()) {
            long time = System.currentTimeMillis() - client.getLastKeepAlive();
            if (time >= 5000) {
                //timed out.
                clientDisconnected(client);
            }
        }

    }

    /**
     * @return true, if the server is running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @return the connection handler.
     */
    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }
}
