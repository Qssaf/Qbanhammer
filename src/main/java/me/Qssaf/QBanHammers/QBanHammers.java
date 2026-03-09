package me.Qssaf.QBanHammers;


import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static me.Qssaf.QBanHammers.ConfigManager.loadHammers;


public final class QBanHammers extends JavaPlugin {

    public static @NotNull QBanHammers getInstance() {
        return getPlugin(QBanHammers.class);
    }
    private static final Float version = 2.6f;


    @Override
    public void onEnable() {

        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new EventManager(), this);
        Objects.requireNonNull(getCommand("qbanhammers")).setExecutor(new CommandManager());
        File file = new File(getInstance().getDataFolder(), "config.yml");
        if (!file.exists()) {
            getLogger().warning("config.yml not found. Adding default config...");
            saveDefaultConfig(); // Saves the default from JAR
        }
        YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(file);
        if (oldConfig.getDouble("ConfigVersion") < version) {
            getLogger().warning("Outdated config.yml detected. Backing up and creating new config...");
            File backupFile = new File(getInstance().getDataFolder(), "config.yml.old." + oldConfig.getDouble("version"));

            if (file.renameTo(backupFile)) {
                getLogger().info("Backup created: " + backupFile.getName());
            } else {
                getLogger().severe("Failed to create backup of config.yml!");
            }
            saveDefaultConfig();
            reloadConfig();
            ConfigManager.loadValues();
        }
        File loggerFile = new File(getInstance().getDataFolder(), "logger.txt");
        if(!file.exists()){
            try {
                loggerFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            getLogger().info("Created Logger for Qbanhammers");
        }
        reloadConfig();
        ConfigManager.loadValues();
        loadHammers();

        getLogger().info("Plugin has been enabled");


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Plugin has shutdown");
    }
}
