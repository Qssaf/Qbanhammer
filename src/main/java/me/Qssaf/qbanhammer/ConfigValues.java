package me.Qssaf.qbanhammer;


import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.Qssaf.qbanhammer.QBanHammers.getInstance;


public class ConfigValues {
    private static final List<NamespacedKey> KEYS = new ArrayList<>();
    public static String prefix = getInstance().getConfig().getString("prefix");
    public static File configFile = new File(getInstance().getDataFolder(), "config.yml");
    private static List<String> hammerlist = new ArrayList<>();
    private static ConfigurationSection hammersSection;

    public static List<NamespacedKey> getKEYS() {
        return KEYS;
    }

    public static List<String> getHammerlist() {
        return hammerlist;
    }

    public static void loadValues() {
        configFile = new File(getInstance().getDataFolder(), "config.yml");
        prefix = getInstance().getConfig().getString("prefix");

    }


    public static void loadHammers() {

        hammersSection = getInstance().getConfig().getConfigurationSection("hammers");
        if (hammersSection != null) {
            hammerlist = hammersSection.getKeys(false).stream().toList();


        }
    }

    public static void registerHammerKeys() {
        KEYS.clear();
        assert hammersSection != null;
        for (String hammer : hammersSection.getKeys(false)) {
            Permission permission = new Permission("qbanhammers.hammers." + hammer, PermissionDefault.OP);
            PluginManager e = QBanHammers.getInstance().getServer().getPluginManager();
            if (e.getPermission(permission.getName()) == null) {
                permission.addParent("qbanhammers.admin", true);
                e.addPermission(permission);
            }
            KEYS.add(new NamespacedKey(QBanHammers.getInstance(), hammer));


        }
    }
}

