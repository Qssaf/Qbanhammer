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

import static me.Qssaf.qbanhammer.configvalues.*;


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
            if (damaged instanceof Player) {
                event.setCancelled(true);
                Bukkit.broadcast(msg);
                attacker.sendMessage(usedhammer);
                Location location = damaged.getLocation();






            } else {

                event.setCancelled(true);
                attacker.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&cYou can't ban a " + damaged.getName().toLowerCase() + "."));
            }
        }
    }
}
