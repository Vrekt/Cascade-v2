package cascade.core.client.session;

public class Session {

    private final String username;
    private final int uniqueId;

    private long ping;

    public Session(String username, int uniqueId) {
        this.username = username;
        this.uniqueId = uniqueId;
    }

    public String getUsername() {
        return username;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public long getPing() {
        return ping;
    }

    public void setPing(long ping) {
        this.ping = ping;
    }

}
