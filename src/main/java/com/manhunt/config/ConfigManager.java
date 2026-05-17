package com.manhunt.config;

import com.manhunt.ManhuntPlugin;

public class ConfigManager {
    private final ManhuntPlugin plugin;
    private final Settings settings;

    public ConfigManager(ManhuntPlugin plugin) {
        this.plugin = plugin;
        this.settings = new Settings(plugin);
    }

    public void init() {
        plugin.saveDefaultConfig();
        settings.load();
    }

    public Settings getSettings() {
        return settings;
    }

    public void reload() {
        plugin.reloadConfig();
        settings.load();
    }
}
