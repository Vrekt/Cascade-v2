package protocol.packets;

import protocol.Packet;
import protocol.connection.AuthenticationType;

import java.io.Serializable;

public class PacketAuthenticationType implements Packet, Serializable {

    private final AuthenticationType type;

    public PacketAuthenticationType(AuthenticationType type) {
        this.type = type;
    }

    public AuthenticationType getType() {
        return type;
    }
}
