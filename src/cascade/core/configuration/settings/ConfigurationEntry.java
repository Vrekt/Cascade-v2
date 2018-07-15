package cascade.core.configuration.settings;

public class ConfigurationEntry {

    private final String name;
    private boolean enabled;
    private int value;

    public ConfigurationEntry(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    public ConfigurationEntry(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getValue() {
        return value;
    }
}
