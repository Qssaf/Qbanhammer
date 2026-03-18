package me.qssaf.qbanhammers.managers;

import me.qssaf.qbanhammers.QBanHammers;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class LoggerManager {
    private static final JavaPlugin plugin = QBanHammers.getInstance();
    private static final File loggerFile = new File(plugin.getDataFolder(), "logger.txt");

    public static void createLogFile() {

        if (!loggerFile.exists()) {
            try {
                loggerFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            plugin.getLogger().info("Created Logger for Qbanhammers");
        }
    }

    public static void write(String message) {
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(QBanHammers.getInstance().getDataFolder().getPath() + "/logger.txt", true))) {

            fileWriter.write(message + "\n");

        } catch (IOException e) {
            QBanHammers.getInstance().getLogger().severe("Could not write to logger.txt: " + e.getMessage());
        }

    }

}
