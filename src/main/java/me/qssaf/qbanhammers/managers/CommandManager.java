package me.qssaf.qbanhammers.managers;

import me.qssaf.qbanhammers.Hammer;
import me.qssaf.qbanhammers.QBanHammers;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.qssaf.qbanhammers.managers.ConfigManager.prefix;
import static me.qssaf.qbanhammers.managers.ConfigManager.text;

public final class CommandManager implements CommandExecutor, TabExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NonNull [] strings) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(text(prefix + QBanHammers.getInstance().getConfig().getString("Console-Gethammer")));
            return true;

        }
        if (strings.length == 0) {
            commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cPlease specify a subcommand!"));
            return true;
        }
        if (strings.length > 2) {
            commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammers.getInstance().getConfig().getString("Invalid-Subcommand")));
            return true;
        } else {

            if (strings[0].equalsIgnoreCase("gethammer")) {
                if (!commandSender.hasPermission("qbanhammers.gethammer")) {
                    commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammers.getInstance().getConfig().getString("No-Permission")));
                    return true;
                }

                if (strings.length > 1) {
                    Optional<Hammer> neededHammer = Hammer.getHammerList().stream().filter((hammer -> hammer.getHammerName().contains(strings[1]))).findFirst();
                    if (neededHammer.isPresent()) {

                        if (!player.hasPermission("qbanhammers.hammers." + strings[1])) {
                            commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammers.getInstance().getConfig().getString("Hammer-NoPermission")));
                            return true;
                        }


                        player.getInventory().addItem(neededHammer.get().getHammerItem());
                        player.sendMessage(text(prefix + Objects.requireNonNull(QBanHammers.getInstance().getConfig().getString("Hammer-recieved")).replace("{hammer}", Objects.requireNonNull(QBanHammers.getInstance().getConfig().getString("hammers." + strings[1] + ".name")))));

                    } else {
                        commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammers.getInstance().getConfig().getString("Invalid-Hammer")));
                    }
                } else {
                    commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammers.getInstance().getConfig().getString("Hammer-Notspecified")));
                }
                return true;


            } else if (strings[0].equalsIgnoreCase("reload")) {
                if (commandSender.hasPermission("qbanhammers.reload")) {
                    QBanHammers.getInstance().saveDefaultConfig();
                    QBanHammers.getInstance().reloadConfig();
                    QBanHammers.getInstance().getServer().getScheduler().runTaskLater(QBanHammers.getInstance(), () -> {
                        ConfigManager.loadValues();
                        ConfigManager.loadHammers();

                        commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammers.getInstance().getConfig().getString("Config-Reloaded")));


                    }, 1L);


                    return true;
                }
            } else {
                commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammers.getInstance().getConfig().getString("Invalid-Subcommand")));
                return true;
            }


        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NonNull [] strings) {
        int length = strings.length;

        if (length == 1) {
            return Stream.of("gethammer", "reload").filter(option -> option.startsWith(strings[0])).filter(option -> commandSender.hasPermission("qbanhammers." + option)).collect(Collectors.toList());


        } else if (length < 3 && strings[0].equalsIgnoreCase("gethammer")) {
            return Hammer.getHammerList().stream()
                    .map(Hammer::getHammerName)
                    .filter(hammerName -> hammerName.startsWith(strings[1]))
                    .filter(hammerName -> commandSender.hasPermission("qbanhammers.hammers." + hammerName))
                    .toList();


        } else {
            return List.of();

        }


    }

}
