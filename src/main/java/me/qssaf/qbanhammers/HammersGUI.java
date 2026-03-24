//package me.qssaf.qbanhammers;
//
//
//import dev.triumphteam.gui.builder.item.ItemBuilder;
//import dev.triumphteam.gui.guis.Gui;
//import dev.triumphteam.gui.guis.GuiItem;
//import me.qssaf.qbanhammers.managers.ConfigManager;
//import net.kyori.adventure.text.Component;
//import org.bukkit.Material;
//import org.bukkit.inventory.ItemStack;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public final class HammersGUI {
//    private static Gui GUI = null;
//
//    public static Gui createGUI() {
//
//        Gui gui = Gui.gui().rows(QBanHammers.getInstance().getConfig().getInt("Menu.rows", 3)).disableItemTake().title(ConfigManager.text("Hammers Menu")).create();
//        gui.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).asGuiItem());
//
//        for (Hammer hammer : Hammer.getHammerList()) {
//            ItemStack hammerItem = new ItemStack(hammer.getHammerItem());
//            List<Component> oldLore = hammerItem.lore();
//            if (oldLore == null) {
//                oldLore = new ArrayList<>();
//            }
//            oldLore.addAll(QBanHammers.getInstance().getConfig().getStringList("Menu.additionalLore").stream().map(ConfigManager::text).toList());
//            hammerItem.lore(oldLore);
//
//            GuiItem hammerGUIItem = ItemBuilder.from(hammerItem).asGuiItem(event -> {
//                event.getWhoClicked().closeInventory();
//                event.getWhoClicked().getInventory().addItem(hammer.getHammerItem());
//            });
//            gui.setItem(QBanHammers.getInstance().getConfig().getInt("hammers." + hammer.getHammerName() + ".slot"), hammerGUIItem);
//        }
//        HammersGUI.GUI = gui;
//        return gui;
//    }
//
//    public static Gui getGui() {
//        if (GUI == null) {
//            GUI = createGUI();
//            return GUI;
//        } else return GUI;
//    }
//
//}
