package me.Qssaf.QBanHammers;


import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static me.Qssaf.QBanHammers.ConfigManager.prefix;


@SuppressWarnings("UnnecessaryReturnStatement")
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
        Optional<Hammer> match = Hammer.getHammerList().stream()
                .filter(hammer -> usedItem.getItemMeta().getPersistentDataContainer().has(hammer.getHammerKey(), PersistentDataType.BOOLEAN))
                .findFirst();


        if (match.isPresent()) {
            NamespacedKey key = match.get().getHammerKey();
            String usedHammer = match.get().getHammerName();

            if (!attacker.hasPermission("qbanhammers.hammers." + usedHammer)) {
                try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(QBanHammers.getInstance().getDataFolder().getPath() + "/logger.txt", true))) {


                    fileWriter.write(QBanHammers.getInstance().getConfig().getString("Logger.noPermission ", "[{date}] {attacker} tried to attack a {attacked} with a {hammer} with no permission.").replace("{attacker}", attacker.getName()).replace("{attacked}", damaged.getName()).replace("{date}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).replace("{hammer}", usedHammer) + "\n");

                } catch (IOException e) {
                    QBanHammers.getInstance().getLogger().severe("Could not write to logger.txt: " + e.getMessage());
                }
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
                    if (QBanHammers.getInstance().getConfig().getBoolean("GameCrasher.enabled", false)) {
                        if (gameCrasherOption.getOrDefault(attacker.getUniqueId(), false) && attacker.hasPermission("qbanhammers.togglegamecrasher")) {
                            Bukkit.getScheduler().runTaskLater(QBanHammers.getInstance(), () -> ((Player) damaged).spawnParticle(Particle.FLAME, location, 2147483647, 10, 10, 10, 0, null, true), (long) (0.3 * 20));
                        }
                    }

                    Bukkit.getScheduler().runTaskLater(QBanHammers.getInstance(), () -> {
                                switch (QBanHammers.getInstance().getConfig().getString("CommandExecutor")) {
                                    case "console":
                                        String command = Objects.requireNonNull(QBanHammers.getInstance().getConfig().getString("hammers." + usedHammer + ".command")).replace("{attacked}", damaged.getName())
                                                .replace("{attacker}", attacker.getName());
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                                        break;
                                    case "player_as_op":
                                        boolean isOp = attacker.isOp();
                                        if (isOp) {
                                            attacker.performCommand(Objects.requireNonNull(QBanHammers.getInstance().getConfig().getString("hammers." + usedHammer + ".command")).replace("{attacked}", damaged.getName())
                                                    .replace("{attacker}", attacker.getName()));
                                        } else {
                                            try {
                                                attacker.setOp(true);
                                                attacker.performCommand(Objects.requireNonNull(QBanHammers.getInstance().getConfig().getString("hammers." + usedHammer + ".command")).replace("{attacked}", damaged.getName())
                                                        .replace("{attacker}", attacker.getName()));
                                                attacker.setOp(false);
                                            } catch (Exception ignored) {
                                            } finally {
                                                attacker.setOp(false);

                                            }

                                        }
                                        break;
                                    case null, default:
                                        attacker.performCommand(Objects.requireNonNull(QBanHammers.getInstance().getConfig().getString("hammers." + usedHammer + ".command")).replace("{attacked}", damaged.getName())
                                                .replace("{attacker}", attacker.getName()));
                                        break;
                                }
                            }
                            , (long) (QBanHammers.getInstance().getConfig().getDouble("hammers." + usedHammer + ".execution-delay", 0.5) * 20));
                    try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(QBanHammers.getInstance().getDataFolder().getPath() + "/logger.txt", true))) {

                        fileWriter.write(QBanHammers.getInstance().getConfig().getString("Logger.struckPlayer", "[{date}] {attacker} has struck {attacked} with a {hammer}.").replace("{attacker}", attacker.getName()).replace("{attacked}", damaged.getName()).replace("{date}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).replace("{hammer}", usedHammer) + "\n");

                    } catch (IOException e) {
                        QBanHammers.getInstance().getLogger().severe("Could not write to logger.txt: " + e.getMessage());
                    }
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
                try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(QBanHammers.getInstance().getDataFolder().getPath() + "/logger.txt", true))) {


                    fileWriter.write(QBanHammers.getInstance().getConfig().getString("Logger.attackedEntity", "[{date}] {attacker} tried to attack a {attacked} with a {hammer}.").replace("{attacker}", attacker.getName()).replace("{attacked}", damaged.getName().toLowerCase()).replace("{date}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).replace("{hammer}", usedHammer) + "\n");

                } catch (IOException e) {
                    QBanHammers.getInstance().getLogger().severe("Could not write to logger.txt: " + e.getMessage());
                }
                attacker.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cYou can't ban a " + damaged.getName().toLowerCase() + "."));
            }
        }
        Bukkit.getScheduler().runTaskLater(QBanHammers.getInstance(), () -> attackedRecently.remove(event.getPlayer().getUniqueId()), 1L);


    }


    @EventHandler
    public void onHammerRightClick(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if (!player.hasPermission("qbanhammers.togglegamecrasher")) {
            return;
        }
        if (attackedRecently.remove(event.getPlayer().getUniqueId())) {
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

        Optional<Hammer> match = Hammer.getHammerList().stream()
                .filter(hammer -> usedItem.getItemMeta().getPersistentDataContainer().has(hammer.getHammerKey(), PersistentDataType.BOOLEAN))
                .findFirst();
        if (match.isPresent()) {

            event.setCancelled(true);

            if (!(QBanHammers.getInstance().getConfig().getBoolean("GameCrasher.enabled", false))) {
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammers.getInstance().getConfig().getString("GameCrasher-Disabled", "&eGame Crasher is disabled from the config.")));
                return;
            }
            boolean currentSetting = gameCrasherOption.getOrDefault(player.getUniqueId(), false);
            gameCrasherOption.put(player.getUniqueId(), !currentSetting);
            String status = !currentSetting ? QBanHammers.getInstance().getConfig().getString("GameCrasher.toggleOnStatus", "&aenabled") : QBanHammers.getInstance().getConfig().getString("GameCrasher.toggleOffStatus", "&cdisabled");
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammers.getInstance().getConfig().getString("GameCrasher-Toggled", "&eGame Crasher option has been {status}&e.").replace("{status}", status)));


        }

    }

    @EventHandler
    public void strikeLightningWhenLeftClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack usedItem = player.getInventory().getItemInMainHand();
        if (usedItem.getType().isAir() || usedItem.isEmpty()) {
            return;
        }
        if (!event.getPlayer().hasPermission("qbanhammers.strikelightning")) {
            return;
        }

        if (!QBanHammers.getInstance().getConfig().getBoolean("StrikeLightningIfNoPlayer", true)) {
            return;
        }


        if (event.getAction() != Action.LEFT_CLICK_AIR &&
                event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }


        Optional<Hammer> match = Hammer.getHammerList().stream()
                .filter(hammer -> usedItem.getItemMeta().getPersistentDataContainer().has(hammer.getHammerKey(), PersistentDataType.BOOLEAN))
                .findFirst();
        if (match.isPresent()) {

            event.setCancelled(true);

            Block block = player.getTargetBlockExact(100);
            Entity targetEntity = player.getTargetEntity(100);
            if (block == null && targetEntity == null) {
                return;
            } else if (block != null && targetEntity != null) {
                Location blocklocation = block.getLocation();
                Location entitylocation = targetEntity.getLocation();
                double blockDist = blocklocation.distance(player.getLocation());
                double entityDist = entitylocation.distance(player.getLocation());
                if (blockDist < entityDist) {

                    blocklocation.add(0.5, 1, 0.5);
                    blocklocation.getWorld().strikeLightningEffect(blocklocation);
                    return;
                } else {
                    if (Objects.requireNonNull(player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE)).getValue() > entityDist) {
                        return;
                    }
                    entitylocation.getWorld().strikeLightningEffect(entitylocation.add(0, -1, 0));

                }

            } else if (targetEntity == null) {
                Location blocklocation = block.getLocation();
                blocklocation.add(0.5, 1, 0.5);
                blocklocation.getWorld().strikeLightningEffect(blocklocation);

            } else {
                Location entitylocation = targetEntity.getLocation();
                if (Objects.requireNonNull(player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE)).getValue() > entitylocation.distance(player.getLocation())) {
                    return;
                }
                entitylocation.getWorld().strikeLightningEffect(entitylocation.add(0, -1, 0));

            }

        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!event.isLeftClick()) {
            return;
        }
        if (!player.hasMetadata("Hammer Menu")) {
            return;
        }
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
            return;
        }
        player.getInventory().addItem(event.getCurrentItem());

        player.sendMessage(ConfigManager.text("&aGiven you a hammer "));
    }

    @EventHandler
    public void onInventoryLeave(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (player.hasMetadata("Hammer Menu")) {
            player.removeMetadata("Hammer Menu", QBanHammers.getInstance());

        }
    }
}
