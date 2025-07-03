package me.Qssaf.qbanhammer;


import com.destroystokyo.paper.event.player.PlayerAttackEntityCooldownResetEvent;
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

import static me.Qssaf.qbanhammer.configvalues.*;


public class Hitevent implements Listener {

    private Component replaceplaceholders(String messageTemplate, Player attacker, Entity damaged) {
        String formatted = messageTemplate
                .replace("{attacked}", damaged.getName())
                .replace("{attacker}", attacker.getName());

        return LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + formatted);
    }


    private final Map<NamespacedKey, UUID> pendingConfirmations = new HashMap<>();



    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerHit(@NotNull PrePlayerAttackEntityEvent event) {


        Entity damaged = event.getAttacked();

        Player attacker = event.getPlayer();
        ItemStack useditem = attacker.getInventory().getItemInMainHand();



        Component msg = replaceplaceholders(strikemsg, attacker, damaged);
        if (attacker.getInventory().getItemInMainHand().getType().isAir() || attacker.getInventory().getItemInMainHand().isEmpty()) {
            return;
        }
        Optional<NamespacedKey> match = configvalues.hammerkeys.stream()
                .filter(key -> useditem.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN))
                .findFirst();





        if(match.isPresent()){
            NamespacedKey key =  match.get();
         String usedhammer = hammerlist.get(configvalues.hammerkeys.indexOf(key));
         if(!attacker.hasPermission(Objects.requireNonNull(Qbanhammer.Getinstance().getConfig().getString("hammers." + usedhammer + ".permission")))){
             attacker.getInventory().setItemInMainHand(ItemStack.of(Material.AIR));
             attacker.sendMessage(replaceplaceholders("&cYou do not have permission to use this hammer!", attacker, damaged));
             event.setCancelled(true);
             return;
         }
            if (damaged instanceof Player) {

                UUID damagedId = damaged.getUniqueId();
                event.setCancelled(true);
                Location location;
                if(pendingConfirmations.containsKey(key) && pendingConfirmations.get(key).equals(damagedId)) {
                    pendingConfirmations.remove(key);
                    Bukkit.broadcast(msg);
                    location = damaged.getLocation();
                    if (location.getWorld() != null) {
                        location.getWorld().strikeLightningEffect(location);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                        }
                    }

                    Bukkit.getScheduler().runTaskLater(Qbanhammer.Getinstance(), () ->
                            attacker.performCommand(Objects.requireNonNull(Qbanhammer.Getinstance().getConfig().getString("hammers." + usedhammer + ".command")).replace("{attacked}", damaged.getName())
                                    .replace("{attacker}", attacker.getName())),10L);

                } else {
                    // Add the player to the pending confirmations
                    pendingConfirmations.put(key, damagedId);
                    attacker.sendMessage(replaceplaceholders("&eYou are about to strike {attacked} with " + usedhammer +". Click again to confirm.", attacker, damaged));
                    Bukkit.getScheduler().runTaskLater(Qbanhammer.Getinstance(), () -> {
                        if(pendingConfirmations.containsKey(key) && pendingConfirmations.get(key).equals(damagedId)) {
                            pendingConfirmations.remove(key);
                            attacker.sendMessage(replaceplaceholders("&cConfirmation timed out", attacker, damaged));
                        }
                    }, 20L * 3); // Remove after 30 seconds
                }





            } else {

                event.setCancelled(true);
                attacker.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cYou can't ban a " + damaged.getName().toLowerCase() + "."));
            }
        }
    }
}
