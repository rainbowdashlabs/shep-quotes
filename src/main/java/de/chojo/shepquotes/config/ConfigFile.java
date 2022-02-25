package de.chojo.shepquotes.config;

import de.chojo.shepquotes.config.elements.BaseSettings;
import de.chojo.shepquotes.config.elements.Database;
import de.chojo.shepquotes.config.elements.PresenceSettings;

@SuppressWarnings("FieldMayBeFinal")
public class ConfigFile {
    private BaseSettings baseSettings = new BaseSettings();
    private PresenceSettings presenceSettings = new PresenceSettings();
    private Database database = new Database();

    public BaseSettings baseSettings() {
        return baseSettings;
    }

    public PresenceSettings presence() {
        return presenceSettings;
    }

    public Database database() {
        return database;
    }
}
