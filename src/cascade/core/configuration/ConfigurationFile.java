package cascade.core.configuration;

import cascade.core.CascadeServer;
import cascade.core.LogLevel;
import cascade.core.configuration.settings.ConfigurationEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ConfigurationFile {
    /**
     * Load the configuration file and read all entries.
     */
    public static void load(File file) {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            CascadeServer.log("Reading configuration file...", LogLevel.INFO);

            // loop through all the lines in the file.
            String nextLine = "";
            while ((nextLine = reader.readLine()) != null) {
                // parse the line
                String entryName = nextLine.split(":")[0];
                String entryValue = nextLine.substring(nextLine.lastIndexOf(" ") + 1);

                boolean enabled = false, isBooleanEntry = false;
                int value = 0;

                try {
                    // check if the value is a number or a boolean
                    value = Integer.parseInt(entryValue);
                } catch (NumberFormatException exception) {
                    // its not a number, parse the bolean
                    isBooleanEntry = true;
                    enabled = Boolean.parseBoolean(entryValue);
                }

                CascadeServer.log("Acquired entry: " + entryName + " enabled: " + enabled + " value: " + value, LogLevel.INFO);
                CascadeServer.getBackend().getConfiguration().addEntry(isBooleanEntry ? new ConfigurationEntry(entryName, enabled) :
                        new ConfigurationEntry(entryName, value));
            }

        } catch (IOException exception) {
            CascadeServer.log("Could not read configuration file! Stopping....", LogLevel.ERROR);
            CascadeServer.stop();
        }
    }

}
