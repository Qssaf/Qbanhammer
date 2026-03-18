package me.qssaf.qbanhammers.managers;

import me.qssaf.qbanhammers.QBanHammers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This is a manager for all methods and functions related to task schedulers, adding Folia support and removing the need keep using "try catch" statements to use the write scheduler.
 */
public final class SchedulerManager {

    private static final boolean isFolia;
    private static final JavaPlugin plugin = QBanHammers.getInstance();

    static {

        isFolia = checkIfHasFoliaSchedulers();
    }

    private SchedulerManager() {
    }

    private static boolean checkIfHasFoliaSchedulers() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isFolia() {
        return isFolia;
    }

    private static JavaPlugin getPlugin() {
        return plugin;
    }


    public static void run(Runnable task) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().execute(getPlugin(), task);
        } else {
            Bukkit.getScheduler().runTask(getPlugin(), task);
        }
    }

    public static void runLater(Runnable task, long delay) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().runDelayed(getPlugin(), (scheduledTask) -> task.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(getPlugin(), task, delay);
        }
    }

    public static void runTimer(Runnable task, long delay, long period) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(getPlugin(), (scheduledTask) -> task.run(), delay, period);
        } else {
            Bukkit.getScheduler().runTaskTimer(getPlugin(), task, delay, period);
        }
    }

    public static void runAsync(Runnable task) {
        if (isFolia) {
            Bukkit.getAsyncScheduler().runNow(getPlugin(), (scheduledTask) -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), task);
        }
    }

    public static void runAtLocation(Runnable task, Location location) {
        if (isFolia) {
            Bukkit.getRegionScheduler().execute(getPlugin(), location, task);
        } else {
            Bukkit.getScheduler().runTask(getPlugin(), task);
        }
    }

    public static void runAtLocationLater(Runnable task, Location location, long delay) {
        if (isFolia) {
            Bukkit.getRegionScheduler().runDelayed(getPlugin(), location, (scheduledTask) -> task.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(getPlugin(), task, delay);
        }
    }

    public static void runAtEntity(org.bukkit.entity.Entity entity, Runnable task) {

        if (isFolia) {
            entity.getScheduler().execute(getPlugin(), task, null, 1L);
        } else {
            // On Paper/Spigot, we just run it on the main thread
            Bukkit.getScheduler().runTask(getPlugin(), task);
        }
    }

    public static void runAtEntityLater(org.bukkit.entity.Entity entity, Runnable task, long delay) {

        if (isFolia) {
            entity.getScheduler().runDelayed(getPlugin(), (scheduledTask) -> task.run(), null, delay);
        } else {
            Bukkit.getScheduler().runTaskLater(getPlugin(), task, delay);
        }
    }

    public static void runAtEntityTimer(org.bukkit.entity.Entity entity, Runnable task, long delay, long period) {

        if (isFolia) {
            entity.getScheduler().runAtFixedRate(getPlugin(), (scheduledTask) -> task.run(), null, delay, period);
        } else {
            Bukkit.getScheduler().runTaskTimer(getPlugin(), task, delay, period);
        }
    }
}