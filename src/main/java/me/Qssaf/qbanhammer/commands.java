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

import java.util.Collections;
import java.util.List;

import static me.Qssaf.qbanhammer.configvalues.*;

public class commands implements CommandExecutor, TabExecutor {
    Component text(String input){
        return LegacyComponentSerializer.legacyAmpersand().deserialize(input);

    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {



        if(strings.length > 1){
            commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cInvalid Subcommand"));
            return true;
        }
        if(strings.length > 0){
            if(!(commandSender instanceof Player player)){
                commandSender.sendMessage("This command can only be run by a player");
                return true;
            }
            if(hammerlist.contains(strings[0])){
                ItemStack hammer = new ItemStack(Material.MACE);
                ItemMeta hammermeta = hammer.getItemMeta();
                hammermeta.displayName(text(Qbanhammer.Getinstance().getConfig().getString("hammers." + strings[0] + ".name")));
                List<Component> lore = List.of(
                        text(Qbanhammer.Getinstance().getConfig().getString("hammers." + strings[0] + ".lore"))
                );
                hammermeta.lore(lore);
                hammermeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                hammermeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                hammermeta.setUnbreakable(true);
                hammermeta.addEnchant(Enchantment.UNBREAKING, 1, true);
                hammermeta.getPersistentDataContainer().set(hammerkeys.get(hammerlist.indexOf(strings[0])), PersistentDataType.BOOLEAN, true);
                hammer.setItemMeta(hammermeta);
                player.getInventory().addItem(hammer);
                player.sendMessage(text("You have been given a " + Qbanhammer.Getinstance().getConfig().getString("hammers." + strings[0] + ".name") + "!"));
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
               return hammerlist;
            }
            else{return List.of();}
        }

        return List.of();
    }
}
