package cascade.core.configuration.settings;

import cascade.core.CascadeServer;
import cascade.core.LogLevel;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

    private List<ConfigurationEntry> entries = new ArrayList<>();

    /**
     * Add an entry.
     *
     * @param entry the entry
     */
    public void addEntry(ConfigurationEntry entry) {
        entries.add(entry);
    }

    /**
     * @param name the name of the entry.
     * @return if the entry is enabled or not.
     */
    public boolean getState(String name) {
        ConfigurationEntry ent = entries.stream().filter(entry -> entry.getName().equals(name)).findAny().orElse(null);
        if (ent != null) {
            return ent.isEnabled();
        }
        CascadeServer.log("Could not find entry: " + name + "!", LogLevel.ERROR);
        return false;
    }

    /**
     * @param name the name of the entry.
     * @return the value of the entry, 0 if no entry was found.
     */
    public int getValue(String name) {
        ConfigurationEntry ent = entries.stream().filter(entry -> entry.getName().equals(name)).findAny().orElse(null);
        if (ent != null) {
            return ent.getValue();
        }
        CascadeServer.log("Could not find entry: " + name + "!", LogLevel.ERROR);
        return 0;
    }

}
