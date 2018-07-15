import protocol.Packet;
import protocol.connection.AuthenticationType;
import protocol.connection.ConnectionStatus;
import protocol.packets.PacketAuthenticationType;
import protocol.packets.PacketConnectionStatus;
import protocol.packets.PacketKeepAlive;
import protocol.packets.PacketMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BasicClient {

    private final String username, server;
    private int port;

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private boolean connected = false;
    private long lastSent;

    private ExecutorService service = Executors.newFixedThreadPool(3);

    public static void main(String[] args) {

        Scanner i = new Scanner(System.in);
        String user = i.nextLine();
        String ip = i.nextLine();

        new BasicClient(user, ip, 1569);
    }

    public BasicClient(String username, String server, int port) {
        this.username = username;
        this.server = server;
        this.port = port;
        start();
    }

    private void start() {
        System.out.println("Attempting to connect to: " + server + ":" + port);
        try {
            socket = new Socket(server, port);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            PacketAuthenticationType authPacket = (PacketAuthenticationType) in.readObject();
            if (authPacket.getType() == AuthenticationType.PASSWORD) {
                // password auth, ask user for input.
                Scanner input = new Scanner(System.in);
                System.out.println("The server requires a password to connect: >> ");
                String password = input.nextLine();

                if (password == null) {
                    System.out.println("Invalid input!");
                    System.exit(0);
                }

                // send the password
                out.writeObject(new PacketMessage(password));

                // now check if we are authorized.
                PacketConnectionStatus statusPacket = (PacketConnectionStatus) in.readObject();
                if (statusPacket.getConnectionStatus() == ConnectionStatus.DISCONNECTED) {
                    // wrong password.
                    System.out.println("Incorrect password!");
                    System.exit(0);
                }

                if (statusPacket.getConnectionStatus() == ConnectionStatus.AUTHENTICATING) {
                    // correct password.
                    // send our username
                    out.writeObject(new PacketMessage(username));
                }

                // now check if we are connected.
                PacketConnectionStatus connectionStatus = (PacketConnectionStatus) in.readObject();
                if (connectionStatus.getConnectionStatus() != ConnectionStatus.CONNECTED) {
                    System.out.println("Could not connect.");
                    System.exit(0);
                }

            }

            if (authPacket.getType() == AuthenticationType.BASIC || authPacket.getType() == AuthenticationType.NONE) {
                out.writeObject(new PacketMessage(username));
                PacketConnectionStatus connectionStatus = (PacketConnectionStatus) in.readObject();
                if (connectionStatus.getConnectionStatus() == ConnectionStatus.AUTHENTICATING) {
                    System.out.println("Authenticating...");
                } else if (connectionStatus.getConnectionStatus() == ConnectionStatus.DISCONNECTED) {
                    System.out.println("Could not connect.");
                    System.exit(0);
                }

                PacketConnectionStatus connectionStatus2 = (PacketConnectionStatus) in.readObject();
                if (connectionStatus2.getConnectionStatus() == ConnectionStatus.CONNECTED) {
                    lastSent = System.currentTimeMillis();
                    connected();
                } else if (connectionStatus2.getConnectionStatus() == ConnectionStatus.DISCONNECTED) {
                    System.out.println("Could not connect.");
                    System.exit(0);
                }

            }

        } catch (IOException | ClassNotFoundException exception) {
            System.out.println("Could not connect.");
            System.exit(0);
        }
    }

    private void connected() {

        System.out.println("Successfully connected to: " + server);
        connected = true;

        service.execute(() -> {
            while (connected) {
                // send keep alive requests.
                if (System.currentTimeMillis() - lastSent >= 1000) {
                    lastSent = System.currentTimeMillis();
                    send(new PacketKeepAlive());
                }
            }
        });

        service.execute(() -> {
            while (connected) {
                // receive data from the server.
                try {
                    Packet packet = (Packet) in.readObject();
                    if (packet instanceof PacketMessage) {
                        PacketMessage packetMessage = (PacketMessage) packet;
                        if (!packetMessage.getUsername().equals(username)) {
                            System.out.println("[" + packetMessage.getUsername() + "]: " + packetMessage.getMessage());
                        }
                    }

                    if (packet instanceof PacketConnectionStatus) {
                        PacketConnectionStatus connectionStatus = (PacketConnectionStatus) packet;
                        if (connectionStatus.getConnectionStatus() == ConnectionStatus.DISCONNECTED) {
                            disconnect();
                        }
                    }

                } catch (IOException | ClassNotFoundException exception) {
                    disconnect();
                }

            }
        });

        service.execute(() -> {
            // finally get input and send.
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (connected) {
                System.out.println("> ");
                try {
                    // sleep if we dont have input yet.
                    while (!reader.ready()) {
                        Thread.sleep(100);
                    }
                    String message = reader.readLine();
                    send(new PacketMessage(message, username));
                } catch (IOException | InterruptedException exception) {
                    System.out.println("There was an error receiving input.");
                }
            }

        });

    }

    /**
     * Send a packet to the server.
     *
     * @param packet the packet.
     */
    private void send(Packet packet) {
        try {
            out.writeObject(packet);
        } catch (IOException exception) {
            // disconnect();
        }
    }

    /**
     * Disconnect from the server.
     */
    private void disconnect() {
        connected = false;
        System.out.println("Lost connection to the server. ");
        service.shutdownNow();
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException exception) {
            // do nothing.
        }

    }

}
