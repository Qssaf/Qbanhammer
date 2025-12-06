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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static me.Qssaf.qbanhammer.ConfigValues.*;


public class EventManager implements Listener {

    private final Map<NamespacedKey, UUID> pendingConfirmations = new HashMap<>();
    private final Map<UUID, Boolean> gameCrasherOption = new HashMap<>();

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
        Optional<NamespacedKey> match = getKEYS().stream()
                .filter(key -> usedItem.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN))
                .findFirst();


        if (match.isPresent()) {
            NamespacedKey key = match.get();
            String usedHammer = getHammerlist().get(getKEYS().indexOf(key));

            if (!attacker.hasPermission("qbanhammer.hammers." + usedHammer)) {
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

                        if(QBanHammer.getInstance().getConfig().getBoolean("hammers." + usedHammer + ".lightning-strike", false)) {
                            location.getWorld().strikeLightningEffect(location);
                        }
                        
                        if(QBanHammer.getInstance().getConfig().getBoolean("hammers." + usedHammer + ".sound.enabled", false)) {
                            String soundName = QBanHammer.getInstance().getConfig().getString("hammers." + usedHammer + ".sound.name", "ENTITY_LIGHTNING_BOLT_THUNDER");
                            float volume = (float) QBanHammer.getInstance().getConfig().getDouble("hammers." + usedHammer + ".sound.volume", 1.0);
                            float pitch = (float) QBanHammer.getInstance().getConfig().getDouble("hammers." + usedHammer + ".sound.pitch", 1.0);
                            Sound sound = Sound.valueOf(soundName.toUpperCase());




                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.playSound(player, sound, volume, pitch);
                                

                            }

                        }
                    if(QBanHammer.getInstance().getConfig().getBoolean("GameCrasher", false))    {
                        if(gameCrasherOption.getOrDefault(attacker.getUniqueId(), false) && attacker.hasPermission("qbanhammer.togglegamecrasher")){
                            Bukkit.getScheduler().runTaskLater(QBanHammer.getInstance(), () -> ((Player) damaged).spawnParticle(Particle.FLAME, location, 2147483647, 10, 10, 10, 0, null, true),(long) (0.3*20));
                        }
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
                            , (long)(QBanHammer.getInstance().getConfig().getDouble("hammers."+ usedHammer +".execution-delay",0.5)*20));

                } else {
                    // Add the player to the pending confirmations
                    pendingConfirmations.put(key, damagedId);
                    attacker.sendMessage(replacePlaceholders(Objects.requireNonNull(QBanHammer.getInstance().getConfig().getString("Confirmation-Message")).replace("{hammer}",usedHammer), attacker, damaged));
                    Bukkit.getScheduler().runTaskLater(QBanHammer.getInstance(), () -> {
                        if (pendingConfirmations.containsKey(key) && pendingConfirmations.get(key).equals(damagedId)) {
                            pendingConfirmations.remove(key);
                            attacker.sendMessage(replacePlaceholders(Objects.requireNonNull(QBanHammer.getInstance().getConfig().getString("Confirmation-Timeout")), attacker, damaged));
                        }
                    },(long) (20 * ( QBanHammer.getInstance().getConfig().getDouble("TimeOutDuration", 3.0))) );
                }


            } else {

                event.setCancelled(true);
                attacker.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cYou can't ban a " + damaged.getName().toLowerCase() + "."));
            }
        }

    }
    @EventHandler
    public void onHammerRightClick(PlayerInteractEvent event){
        Player player = event.getPlayer();
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
            if(player.hasPermission("qbanhammer.togglegamecrasher")){
                if(!(QBanHammer.getInstance().getConfig().getBoolean("GameCrasher", false))){
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammer.getInstance().getConfig().getString("GameCrasher-Disabled","&eGame Crasher is disabled from the config.")));
                    return;
                }
                boolean currentSetting = gameCrasherOption.getOrDefault(player.getUniqueId(), false);
                gameCrasherOption.put(player.getUniqueId(), !currentSetting);
                String status = !currentSetting ? "&aenabled" : "&cdisabled";
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammer.getInstance().getConfig().getString("GameCrasher-Toggled","&eGame Crasher option has been {status}&e.").replace("{status}", status)));
            } else {
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + QBanHammer.getInstance().getConfig().getString("GamerCrasher-NoPermission","&cYou don't have permission to toggle the Game Crasher option.")));
            }

        }

    }
}
