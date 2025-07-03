package me.Qssaf.qbanhammer;


import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;


import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.Scanner;

import static me.Qssaf.qbanhammer.configvalues.*;

public final class Qbanhammer extends JavaPlugin {

    private void isLicence() {
        //We are getting the licence key string from the config

        try{
            //We are defining the url location as a string to begin with using "/raw" after pastebin to get the raw paste.
            String url = "https://pastebin.com/raw/" + key;

            //Here we open a connection with the pastebin url
            URLConnection openConnection = new URL(url).openConnection();
            //We use firefox's key to access the site, this can be changed to chrome for example, but you will need to find the correct key to do so.
            openConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            //Here we are then reading from the webpage
            Scanner scan = new Scanner((new InputStreamReader(openConnection.getInputStream())));
            while(scan.hasNextLine()){
                //We save the first line into a string
                String firstline = scan.nextLine();
                //If the firstline contains the string "true" (this is where you could put your plugin name)
                if(firstline.contains("Qbanhammertest1")){
                    //The string customer would be "CureMe" as that is the second line of the pastebin
                    String whitelistedip = scan.nextLine();

                    if(Objects.requireNonNull(getIP()).equals(whitelistedip)){
                        //If the string customer is equal to the server ip, we can continue

                        getLogger().info("This ip is whitelisted.");
                        String customer = scan.nextLine();
                        this.getLogger().info("This Plugin has been successfully licenced. The user who purchased this product was " + customer + ".");
                        //We return the method not having disabled the plugin
                        return;

                    }
                    else{
                        getLogger().info("This server is not whitelisted.");
                        throw new Exception("This server is not whitelisted. Please contact the author of this plugin to get your server whitelisted.");
                    }
                }
            }
        }catch(Exception e){
            this.getLogger().info("This plugin was not successfully licenced. It has been disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        //We return the method having disabled the plugin because it hasn't already been returned.

    }

    @Override
    public void onEnable() {
        
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new Hitevent() , this);
        Objects.requireNonNull(getCommand("qbanhammer")).setExecutor(new commands());
        if (!configvalues.configFile.exists()) {
            getLogger().warning("config.yml not found. Restoring default config...");
            saveDefaultConfig(); // Saves the default from JAR
            reloadConfig();
        }

        configvalues.loadvalues();

        loadhammers();
        registerHammerKeys();
        getLogger().info("Plugin has loaded");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Plugin has shutdown");
    }

    public static @NotNull Qbanhammer Getinstance(){
        return getPlugin(Qbanhammer.class);
    }
}
