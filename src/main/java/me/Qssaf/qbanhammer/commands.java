package me.Qssaf.qbanhammer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.util.List;

import static me.Qssaf.qbanhammer.Qbanhammer.Getinstance;
import static me.Qssaf.qbanhammer.configvalues.prefix;
public class commands implements CommandExecutor, TabExecutor {
    Component text(String input){
        return LegacyComponentSerializer.legacyAmpersand().deserialize(input);

    }
    List<String> hammerlist = List.of("xrayhammer","kickhammer","cheatinghammer","permahammer");
    List<String> fulllist = List.of("reload","xrayhammer","kickhammer","cheatinghammer","permahammer");
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        ItemStack kickhammer = new ItemStack(Material.MACE);
        ItemMeta kickhammerItemMeta = kickhammer.getItemMeta();

        kickhammerItemMeta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("&#303030&lB&#3F3F3F&lA&#4E4E4E&lN &#6C6C6C&lH&#7B7B7B&lA&#686868&lM&#565656&lM&#434343&lE&#303030&lR &f[Kick]"));
        List<Component> lore = List.of(
                LegacyComponentSerializer.legacyAmpersand().deserialize("&f"),
                LegacyComponentSerializer.legacyAmpersand().deserialize("&7Kick hammer. Click on a player"),
                LegacyComponentSerializer.legacyAmpersand().deserialize("&7to kick them from the server."),
                LegacyComponentSerializer.legacyAmpersand().deserialize(""),
                LegacyComponentSerializer.legacyAmpersand().deserialize("&#303030&l | &#7B7B7BLeft-Click to ban a player!")
        );
        kickhammerItemMeta.lore(lore);
        kickhammerItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        kickhammerItemMeta.setUnbreakable(true);
        kickhammerItemMeta.setCustomModelData(10017);
        kickhammerItemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);

        kickhammerItemMeta.getPersistentDataContainer().set(hammerkeys.KICKHAMMER, PersistentDataType.BOOLEAN, true);
        kickhammer.setItemMeta(kickhammerItemMeta);


        ItemStack xrayhammer = new ItemStack(Material.MACE);
        ItemMeta xrayhammerItemMeta = xrayhammer.getItemMeta();
        xrayhammerItemMeta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("&#A87406&lB&#B98412&lA&#CB941E&lN &#EEB337&lH&#FFC343&lA&#E9AF34&lM&#D49C25&lM&#BE8815&lE&#A87406&lR &f[XRAY]"));
        List<Component> lorexray = List.of(
                LegacyComponentSerializer.legacyAmpersand().deserialize("&f"),
                LegacyComponentSerializer.legacyAmpersand().deserialize("&7Xray ban hammer. Click on a player"),
                LegacyComponentSerializer.legacyAmpersand().deserialize("&7to ban them for xray use."),
                LegacyComponentSerializer.legacyAmpersand().deserialize(""),
                LegacyComponentSerializer.legacyAmpersand().deserialize("&#A87406&l | &#FFC343Left-Click to ban a player!")
        );
        xrayhammerItemMeta.lore(lorexray);
        xrayhammerItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        xrayhammerItemMeta.setUnbreakable(true);
        xrayhammerItemMeta.setCustomModelData(10016);
        xrayhammerItemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        xrayhammerItemMeta.getPersistentDataContainer().set(hammerkeys.XRAYHAMMER, PersistentDataType.BOOLEAN, true);
        xrayhammer.setItemMeta(xrayhammerItemMeta);


        ItemStack cheatinghammer = new ItemStack(Material.MACE);
        ItemMeta cheatinghammerItemMeta = cheatinghammer.getItemMeta();
        cheatinghammerItemMeta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("&#A80606&lB&#B91212&lA&#CB1E1E&lN &#EE3737&lH&#FF4343&lA&#E93434&lM&#D42525&lM&#BE1515&lE&#A80606&lR &f[Cheaters]"));
        List<Component> lorecheating = List.of(
                LegacyComponentSerializer.legacyAmpersand().deserialize("&f"),
                LegacyComponentSerializer.legacyAmpersand().deserialize("&7ban hammer for cheaters. Click on a player"),
                LegacyComponentSerializer.legacyAmpersand().deserialize("&7to ban them for hacking on the server."),
                LegacyComponentSerializer.legacyAmpersand().deserialize(""),
                LegacyComponentSerializer.legacyAmpersand().deserialize("&#A80606&l | &#FF4343Left-Click to ban a player!")
        );
        cheatinghammerItemMeta.lore(lorecheating);
        cheatinghammerItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        cheatinghammerItemMeta.setUnbreakable(true);
        cheatinghammerItemMeta.setCustomModelData(10018);
        cheatinghammerItemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        cheatinghammerItemMeta.getPersistentDataContainer().set(hammerkeys.CHEATINGHAMMER, PersistentDataType.BOOLEAN, true);
        cheatinghammer.setItemMeta(cheatinghammerItemMeta);


        ItemStack permahammer = new ItemStack(Material.MACE);
        ItemMeta permahammerItemMeta = permahammer.getItemMeta();
        permahammerItemMeta.setCustomModelData(10015);
        permahammerItemMeta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("&#37169C&lB&#3E19B0&lA&#451CC4&lN &#5221EB&lH&#5924FF&lA&#5121E6&lM&#481DCE&lM&#401AB5&lE&#37169C&lR &f[Perma]"));
        List<Component> loreperma = List.of(
                LegacyComponentSerializer.legacyAmpersand().deserialize("&f"),
                LegacyComponentSerializer.legacyAmpersand().deserialize("&7Permanent ban hammer. Click on a player"),
                LegacyComponentSerializer.legacyAmpersand().deserialize("&7to ban them permanently from the server."),
                LegacyComponentSerializer.legacyAmpersand().deserialize(""),
                LegacyComponentSerializer.legacyAmpersand().deserialize("&#37169C&l | &#5924FFLeft-Click to ban a player!")
        );
        permahammerItemMeta.lore(loreperma);
        permahammerItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        permahammerItemMeta.setUnbreakable(true);
        permahammerItemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        permahammerItemMeta.getPersistentDataContainer().set(hammerkeys.PERMAHAMMER, PersistentDataType.BOOLEAN, true);
        permahammer.setItemMeta(permahammerItemMeta);





        if(strings.length > 1){
            commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cInvalid Subcommand"));
            return true;
        }
        if(strings.length > 0){
            switch (strings[0]) {
                case "kickhammer" -> {
                    if(!(commandSender instanceof Player player)){
                        commandSender.sendMessage("This command can only be run by a player");
                        return true;
                    }
                    if (player.hasPermission("qbanhammer.kick")) {
                        player.getInventory().addItem(kickhammer);
                    } else {
                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cYou don't have permission to use this hammer."));
                    }
                    return true;
                }
                case "xrayhammer" -> {
                    if(!(commandSender instanceof Player player)){
                        commandSender.sendMessage("This command can only be run by a player");
                        return true;
                    }
                    if (player.hasPermission("qbanhammer.xrayhammer")) {
                        player.getInventory().addItem(xrayhammer);
                    } else {
                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cYou don't have permission to use this hammer."));
                    }
                    return true;
                }
                case "cheatinghammer" -> {
                    if(!(commandSender instanceof Player player)){
                        commandSender.sendMessage("This command can only be run by a player");
                        return true;
                    }
                    if (player.hasPermission("qbanhammer.cheatinghammer")) {
                        player.getInventory().addItem(cheatinghammer);
                    } else {
                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cYou don't have permission to use this hammer."));
                    }
                    return true;
                }
                case "permahammer" -> {
                    if(!(commandSender instanceof Player player)){
                        commandSender.sendMessage("This command can only be run by a player");
                        return true;
                    }
                    if (player.hasPermission("qbanhammer.permahammer")) {
                        player.getInventory().addItem(permahammer);
                    } else {
                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cYou don't have permission to use this hammer."));
                    }
                    return true;
                }
                case "reload" ->{

                    File configFile = new File(Getinstance().getDataFolder(), "config.yml");

                    try {
                        // Try to reload the config
                        Getinstance().reloadConfig();

                        // Validate required config keys (example)


                        commandSender.sendMessage(text(prefix + "&aConfig reloaded successfully."));

                    } catch (Exception e) {
                        commandSender.sendMessage(text(prefix + "&cError reloading config: " + e.getMessage()));
                        Getinstance().getLogger().warning("Config load failed, attempting to restore default config...");

                        // Rename the corrupted config.yml to config.old
                        File backupFile = new File(Getinstance().getDataFolder(), "config.old");
                        if (configFile.exists()) {
                            boolean renamed = configFile.renameTo(backupFile);
                            if (renamed) {
                                commandSender.sendMessage(text(prefix + "&eCorrupted config.yml renamed to config.old."));
                            } else {
                                commandSender.sendMessage(text(prefix + "&cFailed to rename config.yml. Check file permissions."));
                            }
                        }

                        // Restore default config
                        Getinstance().saveDefaultConfig();
                        Getinstance().reloadConfig();

                        commandSender.sendMessage(text(prefix + "&aDefault config restored. Please fix the old config and reload."));
                    }

                    return true;
                }
                default -> {
                    if(!(commandSender instanceof Player player)){
                        commandSender.sendMessage("This command can only be run by a player");
                        return true;
                    }

                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cInvalid Hammer."));
                    return true;
                }
            }
            }

            else {
            if(!(commandSender instanceof Player player)){
                commandSender.sendMessage("This command can only be run by a player");
                return true;
            }
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize( prefix+ "&cInvalid command"));
                return true;
            }

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        int length = strings.length;
        if(s.isEmpty()){
            return List.of();
        }
        if(length > 0){
            if(length == 1){
                if(commandSender.hasPermission("qbanhammer.reload")){
                    return fulllist;
                }
                else if(commandSender.hasPermission("qbahnammer.kick")){
                    return hammerlist;
                }
                else if(commandSender.hasPermission("qbanhammer.xrayhammer")){
                    return hammerlist;
                }
                else if(commandSender.hasPermission("qbanhammer.cheatinghammer")){
                    return hammerlist;
                }
                else if(commandSender.hasPermission("qbanhammer.permahammer")){
                    return hammerlist;
                }
                else{
                    return List.of();
                }
            }
            else{return List.of();}
        }

        return List.of();
    }
}
