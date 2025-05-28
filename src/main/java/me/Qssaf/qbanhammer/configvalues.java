package me.Qssaf.qbanhammer;

import static me.Qssaf.qbanhammer.Qbanhammer.Getinstance;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
public class configvalues {
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
        key = Getinstance().getConfig().getString("license-key");
    }


    public static String loadhammers(){
        for(String hammer : Getinstance().getConfig().getStringList("hammers")){
            return hammer;
        }
        return null;
    }
}

