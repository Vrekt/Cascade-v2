package protocol.packets;

import protocol.Packet;

import java.io.Serializable;

public class PacketMessage implements Packet, Serializable {

    private final String message, username;

    public PacketMessage(String message, String username) {
        this.message = message;
        this.username = username;
    }

    public PacketMessage(String message) {
        this.message = message;
        this.username = "UNKNOWN";
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }
}
