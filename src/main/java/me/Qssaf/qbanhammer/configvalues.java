package me.Qssaf.qbanhammer;



import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import static me.Qssaf.qbanhammer.Qbanhammer.Getinstance;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class configvalues {
    public static List<NamespacedKey> hammerkeys = new ArrayList<>();
    public static List<String> hammerlist = new ArrayList<>();
    public static ConfigurationSection hammersSection;

    public static String getIP() {
        try {
            return new BufferedReader(new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream())).readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String key = null;
    public  static String prefix = Getinstance().getConfig().getString("prefix");
    public static File configFile = new File(Getinstance().getDataFolder(), "config.yml");
    public static String strikemsg =  Getinstance().getConfig().getString("strikemsg");

    public static void loadvalues(){
        strikemsg = Getinstance().getConfig().getString("strikemsg");
        configFile = new File(Getinstance().getDataFolder(), "config.yml");
        prefix = Getinstance().getConfig().getString("prefix");

    }


    public static List<String> loadhammers(){

       hammersSection = Getinstance().getConfig().getConfigurationSection("hammers");
        if (hammersSection != null) {
            int hammerCount = hammersSection.getKeys(false).size();
            hammerlist = hammersSection.getKeys(false).stream().toList();


            return hammerlist;
        }


        return List.of();
    }
    public static void registerHammerKeys() {
        hammerkeys.clear();
        assert hammersSection != null;
        for(String hammer: hammersSection.getKeys(false)) {
            Permission permission = new Permission("qbanhammer." + hammer, PermissionDefault.OP);
            PluginManager e =  Qbanhammer.Getinstance().getServer().getPluginManager();
            if(e.getPermission(permission.getName()) == null){
                permission.addParent("qbanhammer.admin", true);
                e.addPermission(permission);
            }
            hammerkeys.add(new NamespacedKey(Qbanhammer.Getinstance(), hammer));



        }
    }
}

