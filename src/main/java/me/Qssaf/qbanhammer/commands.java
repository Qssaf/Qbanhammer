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
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static me.Qssaf.qbanhammer.configvalues.*;

public class commands implements CommandExecutor, TabExecutor {
    Component text(String input){
        return LegacyComponentSerializer.legacyAmpersand().deserialize(input);

    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {


        if(strings.length == 0){
            commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cPlease specify a subcommand!"));
            return true;
        }
        if(strings.length > 2){
            commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cInvalid Subcommand"));
            return true;
        }

       else {
            if(!(commandSender instanceof Player player)){
                commandSender.sendMessage("This command can only be run by a player");
                return true;
            }
            if(strings[0].equalsIgnoreCase("gethammer")){
                if(strings.length > 1){
                    if(hammerlist.contains(strings[1])){
                        ItemStack hammer = new ItemStack(Material.MACE);
                        ItemMeta hammermeta = hammer.getItemMeta();
                        hammermeta.displayName(text(Qbanhammer.Getinstance().getConfig().getString("hammers." + strings[1] + ".name")));
                        List<Component> lore = List.of(
                                text(Qbanhammer.Getinstance().getConfig().getString("hammers." + strings[1] + ".lore"))
                        );
                        hammermeta.lore(lore);
                        hammermeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        hammermeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        hammermeta.setCustomModelData(Qbanhammer.Getinstance().getConfig().getInt("hammers." + strings[1] + ".custommodeldata"));
                        hammermeta.setUnbreakable(true);
                        hammermeta.addEnchant(Enchantment.UNBREAKING, 1, true);
                        hammermeta.getPersistentDataContainer().set(hammerkeys.get(hammerlist.indexOf(strings[1])), PersistentDataType.BOOLEAN, true);
                        hammer.setItemMeta(hammermeta);
                        Permission permission = new Permission("qbanhammer." + strings[1], PermissionDefault.OP);
                       PluginManager e =  Qbanhammer.Getinstance().getServer().getPluginManager();
                        if(e.getPermission(permission.getName()) == null){
                            e.addPermission(permission);
                        }
                        player.sendMessage(text(prefix + "&aYou have received a " + Qbanhammer.Getinstance().getConfig().getString("hammers." + strings[1] + ".name") + "&a!"));
                        player.getInventory().addItem(hammer);
                    }

                    else{
                        commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cInvalid Hammer Name"));
                    }
                }
                else{
                    commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cPlease specify a hammer name!"));
                }
            }
            else if(strings[0].equalsIgnoreCase("reload")) {
                if (commandSender.hasPermission("qbanhammer.reload")) {
                    Qbanhammer.Getinstance().reloadConfig();
                    configvalues.loadvalues();
                    configvalues.loadhammers();
                    configvalues.registerHammerKeys();
                    commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&aConfiguration reloaded successfully!"));
                }
            } else {
                    commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cInvalid Subcommand"));
                }



    }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        int length = strings.length;
        if(s.isEmpty()){
            return List.of();
        }
        if(length > 0){
            if(length == 1){
               return List.of("gethammer", "reload");
            }
            else if(length < 3 &&strings[0].equalsIgnoreCase("gethammer")){
                return hammerlist.stream()
                        .filter(hammer -> hammer.startsWith(strings[1]))
                        .toList();
            }
            else{
                return List.of();

            }
        }

        return List.of();
    }
}
