package cascade.core.client.connection;

import protocol.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public abstract class Connection {

    protected Socket socket;
    protected ObjectInputStream in;
    protected ObjectOutputStream out;

    protected boolean connected = true;
    protected long lastKeepAlive;

    public void closeConnection() {
        connected = false;
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException exception) {
            // do nothing.
        }
    }

    /**
     * Send a packet.
     *
     * @param packet
     */
    public void write(Packet packet) {
        try {
            out.writeObject(packet);
        } catch (IOException exception) {
            // do nothing.
        }
    }

    /**
     * @return true, if this connection is still connected.
     */
    public boolean isConnected() {
        return connected;
    }

    public long getLastKeepAlive() {
        return lastKeepAlive;
    }

    public void setLastKeepAlive(long lastKeepAlive) {
        this.lastKeepAlive = lastKeepAlive;
    }
}
