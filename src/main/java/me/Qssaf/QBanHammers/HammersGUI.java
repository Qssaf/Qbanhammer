package me.Qssaf.QBanHammers;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class HammersGUI {
    private static Gui gui = null;

    public static Gui createGUI() {
        Gui gui = Gui.gui().rows(QBanHammers.getInstance().getConfig().getInt("Menu.rows", 3)).disableItemTake().title(ConfigManager.text("Hammers Menu")).create();
        gui.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS).asGuiItem());
        for (Hammer hammer : Hammer.getHammerList()) {
            List<Component> Oldlore = hammer.getHammerItem().lore();
            if (Oldlore == null) {
                Oldlore = new ArrayList<>();
            }
            Oldlore.addAll(QBanHammers.getInstance().getConfig().getStringList("Menu.additionalLore").stream().map(ConfigManager::text).toList());
            hammer.getHammerItem().lore(Oldlore);

            GuiItem hammerGUIItem = ItemBuilder.from(hammer.getHammerItem()).asGuiItem(event -> {
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().getInventory().addItem(hammer.getHammerItem());
            });
            gui.setItem(QBanHammers.getInstance().getConfig().getInt("hammers." + hammer.getHammerName() + ".slot"), hammerGUIItem);
        }

        return gui;
    }

    public static Gui getGui() {
        if (gui == null) {
            gui = createGUI();
            return gui;
        } else return gui;
    }

}
