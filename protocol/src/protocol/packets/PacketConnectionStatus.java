package protocol.packets;

import protocol.Packet;
import protocol.connection.ConnectionStatus;

import java.io.Serializable;

public class PacketConnectionStatus implements Packet, Serializable {

    private final ConnectionStatus connectionStatus;

    public PacketConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }
}
