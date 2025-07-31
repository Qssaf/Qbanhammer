package me.Qssaf.qbanhammer;


import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.Qssaf.qbanhammer.ConfigValues.hammerlist;
import static me.Qssaf.qbanhammer.ConfigValues.prefix;


public class EventManager implements Listener {

    private final Map<NamespacedKey, UUID> pendingConfirmations = new HashMap<>();

    private Component replacePlaceholders(String messageTemplate, Player attacker, Entity damaged) {
        String formatted = messageTemplate
                .replace("{attacked}", damaged.getName())
                .replace("{attacker}", attacker.getName());

        return LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + formatted);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerHit(@NotNull PrePlayerAttackEntityEvent event) {


        Entity damaged = event.getAttacked();

        Player attacker = event.getPlayer();
        ItemStack usedItem = attacker.getInventory().getItemInMainHand();


        if (attacker.getInventory().getItemInMainHand().getType().isAir() || attacker.getInventory().getItemInMainHand().isEmpty()) {
            return;
        }
        Optional<NamespacedKey> match = ConfigValues.KEYS.stream()
                .filter(key -> usedItem.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN))
                .findFirst();


        if (match.isPresent()) {
            NamespacedKey key = match.get();
            String usedHammer = hammerlist.get(ConfigValues.KEYS.indexOf(key));

            if (!attacker.hasPermission(Objects.requireNonNull(QBanHammer.getInstance().getConfig().getString("hammers." + usedHammer + ".permission")))) {
                attacker.getInventory().setItemInMainHand(ItemStack.of(Material.AIR));
                attacker.sendMessage(replacePlaceholders(Objects.requireNonNull(QBanHammer.getInstance().getConfig().getString("Hammer-NoPermission")), attacker, damaged));
                event.setCancelled(true);
                return;
            }
            if (damaged instanceof Player) {
                if (QBanHammer.getInstance().getConfig().getStringList("StrikeWhitelist").contains(damaged.getName())) {
                    attacker.sendMessage(replacePlaceholders(Objects.requireNonNull(QBanHammer.getInstance().getConfig().getString("Whitelisted-Player")), attacker, damaged));
                    event.setCancelled(true);
                    return;

                }
                String strikeMsg = QBanHammer.getInstance().getConfig().getString("hammers." + usedHammer + ".strike-msg", "&c{attacked} has been struck by the Ban Hammer." + "!");
                Component msg = replacePlaceholders(strikeMsg, attacker, damaged);
                UUID damagedId = damaged.getUniqueId();
                event.setCancelled(true);
                Location location;
                if (pendingConfirmations.containsKey(key) && pendingConfirmations.get(key).equals(damagedId)) {
                    pendingConfirmations.remove(key);
                    Bukkit.broadcast(msg);
                    location = damaged.getLocation();

                        location.getWorld().strikeLightningEffect(location);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                        }


                    Bukkit.getScheduler().runTaskLater(QBanHammer.getInstance(), () -> {
                                if (QBanHammer.getInstance().getConfig().getBoolean("ExecuteWithConsole", false)) {
                                    String command = Objects.requireNonNull(QBanHammer.getInstance().getConfig().getString("hammers." + usedHammer + ".command")).replace("{attacked}", damaged.getName())
                                            .replace("{attacker}", attacker.getName());
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

                                } else {
                                    attacker.performCommand(Objects.requireNonNull(QBanHammer.getInstance().getConfig().getString("hammers." + usedHammer + ".command")).replace("{attacked}", damaged.getName())
                                            .replace("{attacker}", attacker.getName()));
                                }
                            }
                            , 10L);

                } else {
                    // Add the player to the pending confirmations
                    pendingConfirmations.put(key, damagedId);
                    attacker.sendMessage(replacePlaceholders(Objects.requireNonNull(QBanHammer.getInstance().getConfig().getString("Confirmation-Message")).replace("hammer",usedHammer), attacker, damaged));
                    Bukkit.getScheduler().runTaskLater(QBanHammer.getInstance(), () -> {
                        if (pendingConfirmations.containsKey(key) && pendingConfirmations.get(key).equals(damagedId)) {
                            pendingConfirmations.remove(key);
                            attacker.sendMessage(replacePlaceholders(Objects.requireNonNull(QBanHammer.getInstance().getConfig().getString("Confirmation-Timeout")), attacker, damaged));
                        }
                    }, 20L * 3);
                }


            } else {

                event.setCancelled(true);
                attacker.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cYou can't ban a " + damaged.getName().toLowerCase() + "."));
            }
        }
    }
}
