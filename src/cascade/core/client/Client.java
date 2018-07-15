package cascade.core.client;

import cascade.core.CascadeServer;
import cascade.core.LogLevel;
import cascade.core.client.connection.Connection;
import cascade.core.client.session.Session;
import protocol.Packet;
import protocol.connection.ConnectionStatus;
import protocol.packets.PacketConnectionStatus;
import protocol.packets.PacketKeepAlive;
import protocol.packets.PacketMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client extends Connection {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Session session;

    public Client(Session session, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        this.session = session;
        this.socket = socket;

        this.in = in;
        this.out = out;
    }

    /**
     * Start listening to packets the client is sending.
     */
    public void start() {
        executor.execute(() -> {
            Thread.currentThread().setName(socket.getInetAddress().toString());
            while (connected) {
                // retrieve data from the client.
                try {
                    Packet packet = (Packet) in.readObject();
                    if (packet instanceof PacketMessage) {
                        PacketMessage message = (PacketMessage) packet;
                        String username = message.getUsername();
                        int uniqueId = message.getUniqueId();

                        CascadeServer.log("[" + uniqueId + "] [" + username + "] >> " + message.getMessage(), LogLevel.INFO);
                        CascadeServer.getBackend().sendPacketToAllClients(packet);
                    }

                    if (packet instanceof PacketConnectionStatus) {
                        PacketConnectionStatus status = (PacketConnectionStatus) packet;
                        if (status.getConnectionStatus() == ConnectionStatus.DISCONNECTED) {
                            this.closeConnection();
                        }
                    }

                    if (packet instanceof PacketKeepAlive) {
                        lastKeepAlive = System.currentTimeMillis();
                    }
                } catch (IOException | ClassNotFoundException | ClassCastException e) {
                    // do nothing for now
                }
            }

        });
    }

    @Override
    public void closeConnection() {
        super.closeConnection();
        executor.shutdownNow();
    }

    public Session getSession() {
        return session;
    }
}
