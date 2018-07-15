package cascade.core.authentication;

import cascade.core.CascadeServer;
import cascade.core.LogLevel;
import protocol.Packet;
import protocol.connection.AuthenticationType;
import protocol.connection.ConnectionStatus;
import protocol.packets.PacketAuthenticationType;
import protocol.packets.PacketConnectionStatus;
import protocol.packets.PacketMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ConnectionHandler {

    private final AuthenticationType type;

    public ConnectionHandler(AuthenticationType type) {
        this.type = type;
    }

    /**
     * Connect a new client.
     *
     * @param socket their connection socket.
     */
    public void connectNewClient(Socket socket) {
        Thread.currentThread().setName(socket.getInetAddress().toString());
        CascadeServer.log("Connecting new client...", LogLevel.INFO);
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // notify the client of the type of authentication being used.
            CascadeServer.getBackend().clientAwaitingAuthentication(socket);
            out.writeObject(new PacketAuthenticationType(type));

            if (type == AuthenticationType.PASSWORD) {
                // retrieve the password from the client and check if its correct.
                Packet packet = (Packet) in.readObject();
                if (!(packet instanceof PacketMessage)) {
                    out.writeObject(new PacketConnectionStatus(ConnectionStatus.DISCONNECTED));
                    CascadeServer.log("Kicking client for invalid protocol.", LogLevel.INFO);
                    socket.close();
                    out.close();
                    in.close();
                    return;
                }

                PacketMessage message = (PacketMessage) packet;
                String password = message.getMessage();
                if (CascadeServer.getBackend().checkPassword(password)) {
                    authenticate(socket, in, out, true);
                    return;
                } else {
                    out.writeObject(new PacketConnectionStatus(ConnectionStatus.DISCONNECTED));
                    CascadeServer.log("Kicking client for invalid password.", LogLevel.INFO);
                    socket.close();
                    out.close();
                    in.close();
                    return;
                }
            }

            authenticate(socket, in, out, type == AuthenticationType.BASIC);
        } catch (IOException | ClassNotFoundException exception) {
            CascadeServer.log("Could not connect new client.", LogLevel.INFO);
        }

    }

    /**
     * Authenticate a new client connecting.
     *
     * @param socket         their socket connection.
     * @param in             the data stream
     * @param out            the data stream
     * @param verifyUsername if true the client will be kicked if another username exists.
     */
    private void authenticate(Socket socket, ObjectInputStream in, ObjectOutputStream out, boolean verifyUsername) {
        // tell the client we are authenticating.
        try {
            out.writeObject(new PacketConnectionStatus(ConnectionStatus.AUTHENTICATING));
            Packet packet = (Packet) in.readObject();
            if (!(packet instanceof PacketMessage)) {
                CascadeServer.log("Disconnecting client for invalid protocol.", LogLevel.INFO);
                socket.close();
                in.close();
                out.close();
                return;
            }

            PacketMessage message = (PacketMessage) packet;
            String username = message.getMessage();
            if (username == null) {
                CascadeServer.log("Disconnecting client for invalid username.", LogLevel.INFO);
                socket.close();
                in.close();
                out.close();
                return;
            }

            if (verifyUsername) {
                boolean taken = CascadeServer.getBackend().isUsernameTaken(username);
                if (taken) {
                    CascadeServer.log("Kicking client for duplicate name.", LogLevel.INFO);
                    out.writeObject(new PacketConnectionStatus(ConnectionStatus.DISCONNECTED));
                    out.writeObject(new PacketMessage("You have been kicked because this user already exists.", "", 0));
                    socket.close();
                    in.close();
                    out.close();
                    return;
                }
            }

            // finally create the clients session.
            CascadeServer.getBackend().clientAuthenticated(username, socket, in, out);
        } catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
    }

}
