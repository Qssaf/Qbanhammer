package me.Qssaf.qbanhammer;


import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static me.Qssaf.qbanhammer.QBanHammer.getInstance;


public class ConfigValues {
    private static final List<NamespacedKey> KEYS = new ArrayList<>();
    private static List<String> hammerlist = new ArrayList<>();
    private static ConfigurationSection hammersSection;
    public static String key = null;
    public static String prefix = getInstance().getConfig().getString("prefix");
    public static File configFile = new File(getInstance().getDataFolder(), "config.yml");
    public static List<NamespacedKey> getKEYS() {
        return KEYS;
    }
    public static List<String> getHammerlist(){
        return hammerlist;
    }
    public static String getIP() {
        try {
            return new BufferedReader(new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream())).readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void loadValues() {
        configFile = new File(getInstance().getDataFolder(), "config.yml");
        prefix = getInstance().getConfig().getString("prefix");

    }


    public static void loadHammers() {

        hammersSection = getInstance().getConfig().getConfigurationSection("hammers");
        if (hammersSection != null) {
            int hammerCount = hammersSection.getKeys(false).size();
            hammerlist = hammersSection.getKeys(false).stream().toList();


        }


    }

    public static void registerHammerKeys() {
        KEYS.clear();
        assert hammersSection != null;
        for (String hammer : hammersSection.getKeys(false)) {
            Permission permission = new Permission("qbanhammer.hammers." + hammer, PermissionDefault.OP);
            PluginManager e = QBanHammer.getInstance().getServer().getPluginManager();
            if (e.getPermission(permission.getName()) == null) {
                permission.addParent("qbanhammer.admin", true);
                e.addPermission(permission);
            }
            KEYS.add(new NamespacedKey(QBanHammer.getInstance(), hammer));


        }
    }
}

