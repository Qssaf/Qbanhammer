package me.Qssaf.QBanHammers;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

import static me.Qssaf.QBanHammers.QBanHammers.getInstance;


public class ConfigManager {

    public static String prefix = getInstance().getConfig().getString("prefix");
    public static File configFile = new File(getInstance().getDataFolder(), "config.yml");


    public static void loadValues() {
        configFile = new File(getInstance().getDataFolder(), "config.yml");
        prefix = getInstance().getConfig().getString("prefix");


    }

    public static Component text(String input) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(input);

    }

    public static void loadHammers() {
        Hammer.clearHammerList();
        ConfigurationSection hammersSection = getInstance().getConfig().getConfigurationSection("hammers");
        if (hammersSection != null) {
            for (String hammerName : hammersSection.getKeys(false).stream().toList()) {
                new Hammer(hammerName);
            }


        } else getInstance().getLogger().severe("No hammers exist in the config");

    }


    }


