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
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.Qssaf.qbanhammer.configvalues.prefix;
import static me.Qssaf.qbanhammer.configvalues.strikemsg;


public class Hitevent implements Listener {

    private Component replaceplaceholders(String messageTemplate, Player attacker, Entity damaged) {
        String formatted = messageTemplate
                .replace("{attacked}", damaged.getName())
                .replace("{attacker}", attacker.getName());

        return LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + formatted);
    }


    private final Map<UUID, UUID> pendingConfirmationskick = new HashMap<>();
    private final Map<UUID, UUID> pendingConfirmationsxray = new HashMap<>();
    private final Map<UUID, UUID> pendingConfirmationscheating = new HashMap<>();
    private final Map<UUID, UUID> pendingConfirmationsperma = new HashMap<>();
    private final Map<UUID, Boolean> pendingConfirmationstimeout = new HashMap<>();


    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerHit(@NotNull PrePlayerAttackEntityEvent event) {

        Entity damaged = event.getAttacked();

        Player attacker = event.getPlayer();



        Component msg = replaceplaceholders(strikemsg, attacker, damaged);
        if (attacker.getInventory().getItemInMainHand().getType().isAir() || attacker.getInventory().getItemInMainHand().isEmpty()) {
            return;
        }

        if (damaged instanceof Player) {
            Location location;
            UUID attackerId = attacker.getUniqueId();
            UUID damagedId = damaged.getUniqueId();

            if (attacker.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(hammerkeys.KICKHAMMER)) {
                event.setCancelled(true);
                if (!attacker.hasPermission("qbanhammer.kickhammer")) {
                    attacker.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cYou don't have permission to use this hammer."));
                    attacker.getInventory().setItemInMainHand(ItemStack.of(Material.AIR));
                    return;
                }


                if (pendingConfirmationskick.containsKey(attackerId) && pendingConfirmationskick.get(attackerId).equals(damagedId)) {
                    pendingConfirmationskick.remove(attackerId);
                    pendingConfirmationstimeout.remove(attackerId);

                    Bukkit.broadcast(msg);
                    location = damaged.getLocation();
                    if (location.getWorld() != null) {
                        location.getWorld().strikeLightningEffect(location);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                        }
                    }

                    Bukkit.getScheduler().runTaskLater(Qbanhammer.Getinstance(), () ->
                            attacker.performCommand("kick " + damaged.getName()), 10L);

                } else {
                    pendingConfirmationskick.put(attackerId, damagedId);
                    attacker.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&eReady to kick " + damaged.getName() + ". Hit again within 5 seconds to confirm."));
                    pendingConfirmationstimeout.put(attackerId, true);

                    // Clear after 5 seconds
                    Bukkit.getScheduler().runTaskLater(Qbanhammer.Getinstance(), () -> {
                        pendingConfirmationskick.remove(attackerId);

                        if (pendingConfirmationstimeout.containsKey(attackerId) && pendingConfirmationstimeout.get(attackerId).equals(true)) {
                            pendingConfirmationstimeout.remove(attackerId);
                            attacker.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cKick Confirmation timed out."));
                        }
                    }, 100L);


                }


            } else if (attacker.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(hammerkeys.XRAYHAMMER)) {
                event.setCancelled(true);
                if (!attacker.hasPermission("qbanhammer.xrayhammer")) {
                    attacker.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cYou don't have permission to use this hammer."));
                    attacker.getInventory().setItemInMainHand(ItemStack.of(Material.AIR));
                    return;
                }
                if (pendingConfirmationsxray.containsKey(attackerId) && pendingConfirmationsxray.get(attackerId).equals(damagedId)) {
                    pendingConfirmationsxray.remove(attackerId);
                    pendingConfirmationstimeout.remove(attackerId);
                    Bukkit.broadcast(msg);
                    location = damaged.getLocation();
                    if (location.getWorld() != null) {
                        location.getWorld().strikeLightningEffect(location);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                        }
                    }

                    Bukkit.getScheduler().runTaskLater(Qbanhammer.Getinstance(), () ->
                            attacker.performCommand("ban " + damaged.getName() + " 30d Xraying"), 10L);

                } else {
                    pendingConfirmationsxray.put(attackerId, damagedId);
                    attacker.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&eReady to ban " + damaged.getName() + ". Hit again within 5 seconds to confirm."));
                    pendingConfirmationstimeout.put(attackerId, true);

                    // Clear after 5 seconds
                    Bukkit.getScheduler().runTaskLater(Qbanhammer.Getinstance(), () -> {
                        pendingConfirmationsxray.remove(attackerId);

                        if (pendingConfirmationstimeout.containsKey(attackerId) && pendingConfirmationstimeout.get(attackerId).equals(true)) {
                            pendingConfirmationstimeout.remove(attackerId);
                            attacker.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cXray Confirmation timed out."));
                        }
                    }, 100L);


                }
            } else if (attacker.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(hammerkeys.CHEATINGHAMMER)) {
                event.setCancelled(true);
                if (!attacker.hasPermission("qbanhammer.cheatinghammer")) {
                    attacker.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cYou don't have permission to use this hammer."));
                    attacker.getInventory().setItemInMainHand(ItemStack.of(Material.AIR));
                    return;
                }
                if (pendingConfirmationscheating.containsKey(attackerId) && pendingConfirmationscheating.get(attackerId).equals(damagedId)) {
                    pendingConfirmationscheating.remove(attackerId);
                    pendingConfirmationstimeout.remove(attackerId);
                    Bukkit.broadcast(msg);
                    location = damaged.getLocation();
                    if (location.getWorld() != null) {
                        location.getWorld().strikeLightningEffect(location);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                        }
                    }

                    Bukkit.getScheduler().runTaskLater(Qbanhammer.Getinstance(), () ->
                            attacker.performCommand("ban " + damaged.getName() + " 30d Cheating"), 10L);

                } else {
                    pendingConfirmationscheating.put(attackerId, damagedId);
                    attacker.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&eReady to ban " + damaged.getName() + ". Hit again within 5 seconds to confirm."));
                    pendingConfirmationstimeout.put(attackerId, true);

                    // Clear after 5 seconds
                    Bukkit.getScheduler().runTaskLater(Qbanhammer.Getinstance(), () -> {
                        pendingConfirmationscheating.remove(attackerId);

                        if (pendingConfirmationstimeout.containsKey(attackerId) && pendingConfirmationstimeout.get(attackerId).equals(true)) {
                            pendingConfirmationstimeout.remove(attackerId);
                            attacker.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cCheating Confirmation timed out."));
                        }
                    }, 100L);


                }
            } else if (attacker.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(hammerkeys.PERMAHAMMER)) {
                event.setCancelled(true);
                if (!attacker.hasPermission("qbanhammer.permahammer")) {
                    attacker.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cYou don't have permission to use this hammer."));
                    attacker.getInventory().setItemInMainHand(ItemStack.of(Material.AIR));
                    return;
                }

                if (pendingConfirmationsperma.containsKey(attackerId) && pendingConfirmationsperma.get(attackerId).equals(damagedId)) {
                    pendingConfirmationsperma.remove(attackerId);
                    pendingConfirmationstimeout.remove(attackerId);
                    Bukkit.broadcast(msg);
                    location = damaged.getLocation();
                    if (location.getWorld() != null) {
                        location.getWorld().strikeLightningEffect(location);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                        }
                    }

                    Bukkit.getScheduler().runTaskLater(Qbanhammer.Getinstance(), () ->
                            attacker.performCommand("banip " + damaged.getName() + " Ban evading."), 10L);

                } else {
                    pendingConfirmationsperma.put(attackerId, damagedId);
                    attacker.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&eReady to ban " + damaged.getName() + ", Hit again within 5 seconds to confirm."));
                    pendingConfirmationstimeout.put(attackerId, true);

                    // Clear after 5 seconds
                    Bukkit.getScheduler().runTaskLater(Qbanhammer.Getinstance(), () -> {
                        pendingConfirmationsperma.remove(attackerId);

                        if (pendingConfirmationstimeout.containsKey(attackerId) && pendingConfirmationstimeout.get(attackerId).equals(true)) {
                            pendingConfirmationstimeout.remove(attackerId);
                            attacker.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cPerma Confirmation timed out."));
                        }
                    }, 100L);


                }
            }


        } else if (attacker.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(hammerkeys.KICKHAMMER) || attacker.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(hammerkeys.XRAYHAMMER) || attacker.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(hammerkeys.PERMAHAMMER) || attacker.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(hammerkeys.CHEATINGHAMMER)) {

            event.setCancelled(true);
            attacker.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cYou can't ban a " + damaged.getName().toLowerCase() + "."));
        }
    }
}
