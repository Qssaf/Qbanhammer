package me.Qssaf.qbanhammer;


import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

import static me.Qssaf.qbanhammer.ConfigValues.*;


public class EventManager implements Listener {
    private final Set<UUID> attackedRecently = new HashSet<>();

    private final Map<NamespacedKey, UUID> pendingConfirmations = new HashMap<>();
    private final Map<UUID, Boolean> gameCrasherOption = new HashMap<>();

    private Component replacePlaceholders(String messageTemplate, Player attacker, Entity damaged) {
        String formatted = messageTemplate
                .replace("{attacked}", damaged.getName())
                .replace("{attacker}", attacker.getName());

        return LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + formatted);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerHit(PrePlayerAttackEntityEvent event) {
        attackedRecently.add(event.getPlayer().getUniqueId());

        Entity damaged = event.getAttacked();

        Player attacker = event.getPlayer();
        ItemStack usedItem = attacker.getInventory().getItemInMainHand();


        if (attacker.getInventory().getItemInMainHand().getType().isAir() || attacker.getInventory().getItemInMainHand().isEmpty()) {
            return;
        }
        Optional<NamespacedKey> match = getKEYS().stream()
                .filter(key -> usedItem.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN))
                .findFirst();


        if (match.isPresent()) {
            NamespacedKey key = match.get();
            String usedHammer = getHammerlist().get(getKEYS().indexOf(key));

            if (!attacker.hasPermission("qbanhammers.hammers." + usedHammer)) {
                attacker.getInventory().setItemInMainHand(ItemStack.of(Material.AIR));
                attacker.sendMessage(replacePlaceholders(Objects.requireNonNull(QBanHammers.getInstance().getConfig().getString("Hammer-NoPermission")), attacker, damaged));
                event.setCancelled(true);
                return;
            }
            if (damaged instanceof Player) {
                if (QBanHammers.getInstance().getConfig().getStringList("StrikeWhitelist").contains(damaged.getName())) {
                    attacker.sendMessage(replacePlaceholders(QBanHammers.getInstance().getConfig().getString("Whitelisted-Player", "&c{damaged} is immune to Ban Hammers."), attacker, damaged));
                    event.setCancelled(true);
                    return;

                }
                String strikeMsg = QBanHammers.getInstance().getConfig().getString("hammers." + usedHammer + ".strike-msg", "&c{attacked} has been struck by the Ban Hammer." + "!");
                Component msg = replacePlaceholders(strikeMsg, attacker, damaged);
                UUID damagedId = damaged.getUniqueId();
                event.setCancelled(true);
                Location location;
                if (pendingConfirmations.containsKey(key) && pendingConfirmations.get(key).equals(damagedId)) {
                    pendingConfirmations.remove(key);
                    Bukkit.broadcast(msg);
                    location = damaged.getLocation();

                    if (QBanHammers.getInstance().getConfig().getBoolean("hammers." + usedHammer + ".lightning-strike", false)) {
                        location.getWorld().strikeLightningEffect(location);
                    }

                    if (QBanHammers.getInstance().getConfig().getBoolean("hammers." + usedHammer + ".sound.enabled", false)) {
                        String soundName = QBanHammers.getInstance().getConfig().getString("hammers." + usedHammer + ".sound.name", "ENTITY_LIGHTNING_BOLT_THUNDER");
                        float volume = (float) QBanHammers.getInstance().getConfig().getDouble("hammers." + usedHammer + ".sound.volume", 1.0);
                        float pitch = (float) QBanHammers.getInstance().getConfig().getDouble("hammers." + usedHammer + ".sound.pitch", 1.0);
                        Sound sound = Sound.valueOf(soundName.toUpperCase());


                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player, sound, volume, pitch);


                        }

                    }
                    if (QBanHammers.getInstance().getConfig().getBoolean("GameCrasher", false)) {
                        if (gameCrasherOption.getOrDefault(attacker.getUniqueId(), false) && attacker.hasPermission("qbanhammers.togglegamecrasher")) {
                            Bukkit.getScheduler().runTaskLater(QBanHammers.getInstance(), () -> ((Player) damaged).spawnParticle(Particle.FLAME, location, 2147483647, 10, 10, 10, 0, null, true), (long) (0.3 * 20));
                        }
                    }

                    Bukkit.getScheduler().runTaskLater(QBanHammers.getInstance(), () -> {
                                if (QBanHammers.getInstance().getConfig().getBoolean("ExecuteWithConsole", false)) {
                                    String command = Objects.requireNonNull(QBanHammers.getInstance().getConfig().getString("hammers." + usedHammer + ".command")).replace("{attacked}", damaged.getName())
                                            .replace("{attacker}", attacker.getName());
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

                                } else {
                                    attacker.performCommand(Objects.requireNonNull(QBanHammers.getInstance().getConfig().getString("hammers." + usedHammer + ".command")).replace("{attacked}", damaged.getName())
                                            .replace("{attacker}", attacker.getName()));
                                }
                            }
                            , (long) (QBanHammers.getInstance().getConfig().getDouble("hammers." + usedHammer + ".execution-delay", 0.5) * 20));

                } else {
                    // Add the player to the pending confirmations
                    pendingConfirmations.put(key, damagedId);
                    attacker.sendMessage(replacePlaceholders(Objects.requireNonNull(QBanHammers.getInstance().getConfig().getString("Confirmation-Message")).replace("{hammer}", usedHammer), attacker, damaged));
                    Bukkit.getScheduler().runTaskLater(QBanHammers.getInstance(), () -> {
                        if (pendingConfirmations.containsKey(key) && pendingConfirmations.get(key).equals(damagedId)) {
                            pendingConfirmations.remove(key);
                            attacker.sendMessage(replacePlaceholders(Objects.requireNonNull(QBanHammers.getInstance().getConfig().getString("Confirmation-Timeout")), attacker, damaged));
                        }
                    }, (long) (20 * (QBanHammers.getInstance().getConfig().getDouble("TimeOutDuration", 3.0))));
                }


            } else {

                event.setCancelled(true);
                attacker.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cYou can't ban a " + damaged.getName().toLowerCase() + "."));
            }
        }
        Bukkit.getScheduler().runTaskLater(QBanHammers.getInstance(), () -> attackedRecently.remove(event.getPlayer().getUniqueId()),1L);


    }


    @EventHandler
    public void onHammerRightClick(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if (!player.hasPermission("qbanhammers.togglegamecrasher")) {
            return;
        }
        if(attackedRecently.remove(event.getPlayer().getUniqueId())){
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) {

            return;
        }
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }
        ItemStack usedItem = player.getInventory().getItemInMainHand();

        if (usedItem.getType().isAir() || usedItem.isEmpty()) {
            return;
        }

        Optional<NamespacedKey> match = getKEYS().stream()
                .filter(key -> usedItem.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN))
                .findFirst();
        if (match.isPresent()) {

            event.setCancelled(true);
            if (player.hasPermission("qbanhammers.togglegamecrasher")) {
                if (!(QBanHammers.getInstance().getConfig().getBoolean("GameCrasher", false))) {
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammers.getInstance().getConfig().getString("GameCrasher-Disabled", "&eGame Crasher is disabled from the config.")));
                    return;
                }
                boolean currentSetting = gameCrasherOption.getOrDefault(player.getUniqueId(), false);
                gameCrasherOption.put(player.getUniqueId(), !currentSetting);
                String status = !currentSetting ? "&aenabled" : "&cdisabled";
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammers.getInstance().getConfig().getString("GameCrasher-Toggled", "&eGame Crasher option has been {status}&e.").replace("{status}", status)));
            } else {
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammers.getInstance().getConfig().getString("GamerCrasher-NoPermission", "&cYou don't have permission to toggle the Game Crasher option.")));
            }

        }

    }

    @EventHandler
    public void StrikeLightningWhenLeftClick(PlayerInteractEvent event) {

        if (!QBanHammers.getInstance().getConfig().getBoolean("StrikeLightningIfNoPlayer", true)) {
            return;
        }
        if (!event.getPlayer().hasPermission("qbanhammers.strikelightning")) {
            return;
        }
        Player player = event.getPlayer();
        if (event.getAction() != Action.LEFT_CLICK_AIR &&
                event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        ItemStack usedItem = player.getInventory().getItemInMainHand();
        if (usedItem.getType().isAir() || usedItem.isEmpty()) {
            return;
        }
        Optional<NamespacedKey> match = getKEYS().stream()
                .filter(key -> usedItem.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN))
                .findFirst();
        if (match.isPresent()) {

            event.setCancelled(true);

            Block block = player.getTargetBlockExact(100);

            Entity TargetEntity = player.getTargetEntity(100);
            if (block == null && TargetEntity == null) {
                return;
            }
            if (TargetEntity == null) {
                Location blocklocation = block.getLocation();
                blocklocation.add(0.5, 1, 0.5);
                blocklocation.getWorld().strikeLightningEffect(blocklocation);
                return;
            }
            if (block == null) {
                Location entitylocation = TargetEntity.getLocation();

                entitylocation.getWorld().strikeLightningEffect(entitylocation.add(0, -1, 0));
                return;
            }
            Location blocklocation = block.getLocation();
            Location entitylocation = TargetEntity.getLocation();
            double blockDist = blocklocation.distance(player.getLocation());
            double entityDist = entitylocation.distance(player.getLocation());
            if (entitylocation.distance(player.getLocation()) < 5) {
                return;
            }
            if (blockDist < entityDist) {

                blocklocation.add(0.5, 1, 0.5);
                blocklocation.getWorld().strikeLightningEffect(blocklocation);

            } else {
                entitylocation.getWorld().strikeLightningEffect(entitylocation.add(0, -1, 0));

            }


        }
    }
}
