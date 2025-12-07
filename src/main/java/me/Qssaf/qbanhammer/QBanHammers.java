package me.Qssaf.qbanhammer;


import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static me.Qssaf.qbanhammer.ConfigValues.*;

public final class QBanHammers extends JavaPlugin {

    public static @NotNull QBanHammers getInstance() {
        return getPlugin(QBanHammers.class);
    }


    @Override
    public void onEnable() {

        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new EventManager(), this);
        Objects.requireNonNull(getCommand("qbanhammer")).setExecutor(new Commands());
        if (!ConfigValues.configFile.exists()) {
            getLogger().warning("config.yml not found. Restoring default config...");
            saveDefaultConfig(); // Saves the default from JAR

        }
        reloadConfig();
        ConfigValues.loadValues();

        loadHammers();
        registerHammerKeys();
        getLogger().info("Plugin has loaded");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Plugin has shutdown");
    }
}
