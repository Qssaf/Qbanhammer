package me.Qssaf.qbanhammer;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.Qssaf.qbanhammer.ConfigValues.*;

public class Commands implements CommandExecutor, TabExecutor {
    Component text(String input) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(input);

    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {


        if (strings.length == 0) {
            commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cPlease specify a subcommand!"));
            return true;
        }
        if (strings.length > 2) {
            commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammer.getInstance().getConfig().getString("Invalid-Subcommand")));
            return true;
        } else {

            if (strings[0].equalsIgnoreCase("gethammer")) {
                if (!commandSender.hasPermission("qbanhammer.gethammer")) {
                    commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammer.getInstance().getConfig().getString("No-Permission")));
                    return true;
                }
                if (!(commandSender instanceof Player player)) {
                    commandSender.sendMessage(text(prefix + QBanHammer.getInstance().getConfig().getString("Console-Gethammer")));
                    return true;
                }
                if (strings.length > 1) {
                    if (getHammerlist().contains(strings[1])) {
                        if (!player.hasPermission("qbanhammer.hammers." + strings[1])) {
                            commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammer.getInstance().getConfig().getString("Hammer-NoPermission")));
                            return true;
                        }

                        ItemStack hammer = new ItemStack(Material.MACE);
                        ItemMeta hammermeta = hammer.getItemMeta();
                        hammermeta.displayName(text(QBanHammer.getInstance().getConfig().getString("hammers." + strings[1] + ".name")));
                        List<Component> lore = new ArrayList<>();
                        for (String line : QBanHammer.getInstance().getConfig().getStringList("hammers." + strings[1] + ".lore")) {
                            lore.add(text(line));
                        }
                        try{
                        hammermeta.lore(lore);
                        hammermeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        hammermeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        String modelData = QBanHammer.getInstance().getConfig().getString("hammers." + strings[1] + ".modeldata","0");

                        if(modelData.startsWith("ia:"))
                        {
                            modelData = modelData.replace("ia:", "");
                            CustomStack stack = CustomStack.getInstance(modelData);
                            if(stack != null)
                            {
                                hammermeta.setCustomModelData(stack.getItemStack().getItemMeta().getCustomModelData());
                            }
                            else {
                                player.sendMessage(text(prefix + QBanHammer.getInstance().getConfig().getString("ModelData-Error","&cInvalid model data for hammer {hamer}" ).replace("{hammer}", strings[1])));
                            }
                        }
                            else if (modelData.startsWith("nexo:")) {
                                modelData = modelData.replace("nexo:", "");
                                ItemBuilder nexoItem = NexoItems.itemFromId(modelData);
                                if (nexoItem != null) {
                                    hammermeta.setCustomModelData(nexoItem.build().getItemMeta().getCustomModelData());

                                } else {
                                    player.sendMessage(text(prefix + QBanHammer.getInstance().getConfig().getString("ModelData-Error","&cInvalid model data for hammer {hamer}" ).replace("{hammer}", strings[1])));
                                }
                            }
                         else{
                            try {
                                hammermeta.setCustomModelData(Integer.parseInt(modelData));
                            } catch (NumberFormatException e) {
                                player.sendMessage(text(prefix + QBanHammer.getInstance().getConfig().getString("ModelData-Error","&cInvalid model data for hammer {hamer}" ).replace("{hammer}", strings[1])));
                            }
                        }

                        hammermeta.setUnbreakable(true);
                        hammermeta.addEnchant(Enchantment.UNBREAKING, 1, true);
                        hammermeta.getPersistentDataContainer().set(getKEYS().get(getHammerlist().indexOf(strings[1])), PersistentDataType.BOOLEAN, true);
                        hammer.setItemMeta(hammermeta);

                        player.sendMessage(text(prefix + Objects.requireNonNull(QBanHammer.getInstance().getConfig().getString("Hammer-recieved")).replace("{hammer}", Objects.requireNonNull(QBanHammer.getInstance().getConfig().getString("hammers." + strings[1] + ".name")))));
                        player.getInventory().addItem(hammer);
                        }
                        catch (Exception e){
                            player.sendMessage(text(prefix + "&cAn error occurred while giving you the hammer. Please recheck the QBanHammer.getInstance().getConfig()uration of the hammer.."));
                            e.printStackTrace();
                        }
                    } else {
                        commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammer.getInstance().getConfig().getString("Invalid-Hammer")));
                        return true;
                    }
                    return true;
                } else {
                    commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammer.getInstance().getConfig().getString("Hammer-Notspecified")));
                    return true;
                }
            }  if (strings[0].equalsIgnoreCase("reload")) {
                if (commandSender.hasPermission("qbanhammer.reload")) {
                    QBanHammer.getInstance().saveDefaultConfig();
                    QBanHammer.getInstance().reloadConfig();
                    QBanHammer.getInstance().getServer().getScheduler().runTaskLater(QBanHammer.getInstance(), () -> {
                        ConfigValues.loadValues();
                        ConfigValues.loadHammers();
                        ConfigValues.registerHammerKeys();
                        commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammer.getInstance().getConfig().getString("Config-Reloaded")));


                    }, 1L);



                    return true;
                }
            } else {
                commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammer.getInstance().getConfig().getString("Invalid-Subcommand")));
                return true;
            }


        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        int length = strings.length;
        if (s.isEmpty()) {
            return List.of();
        }
        if (length > 0) {
            if (length == 1) {
                return Stream.of("gethammer", "reload").filter(option -> option.startsWith(strings[0])).filter(option -> commandSender.hasPermission("qbanhammer."+option)).collect(Collectors.toList());


            } else if (length < 3 && strings[0].equalsIgnoreCase("gethammer")) {
                return getHammerlist().stream()
                        .filter(hammer -> hammer.startsWith(strings[1])).filter(hammer -> commandSender.hasPermission("qbanhammer.hammers." + hammer))
                        .toList();
            } else {
                return List.of();

            }
        }

        return List.of();
    }
}
