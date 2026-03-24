package me.qssaf.qbanhammers;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.qssaf.qbanhammers.managers.ConfigManager.prefix;
import static me.qssaf.qbanhammers.managers.ConfigManager.text;

public class Hammer {
    private static final List<Hammer> hammerList = new ArrayList<>();

    private final QBanHammers plugin = QBanHammers.getInstance();
    private ItemStack hammerItem;
    private String hammerName;
    private NamespacedKey hammerKey;

    public Hammer(Hammer hammer) {
        this.hammerItem = new ItemStack(hammer.getHammerItem());
        this.hammerName = hammer.getHammerName();
        this.hammerKey = hammer.getHammerKey();

    }

    public Hammer(String hammerName) {

        if (!plugin.getConfig().contains("hammers." + hammerName)) {
            plugin.getLogger().severe("Unable to load Hammer: " + hammerName);
            plugin.getLogger().severe("Reason: Couldn't find {hammer} in the config".replace("{hammer}", hammerName));
            return;
        }
        this.hammerName = hammerName;

        ItemStack hammerItem = new ItemStack(Material.MACE);
        ItemMeta hammerMeta = hammerItem.getItemMeta();
        hammerMeta.displayName(text(QBanHammers.getInstance().getConfig().getString("hammers." + hammerName + ".name")));
        List<Component> lore = new ArrayList<>();
        for (String line : QBanHammers.getInstance().getConfig().getStringList("hammers." + hammerName + ".lore")) {
            lore.add(text(line));
        }

        hammerMeta.lore(lore);
        hammerMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        hammerMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        String modelData = QBanHammers.getInstance().getConfig().getString("hammers." + hammerName + ".model-data", "0");

        hammerMeta.setUnbreakable(true);
        hammerMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        NamespacedKey hammerKey = new NamespacedKey(plugin, this.hammerName);
        hammerMeta.getPersistentDataContainer().set(hammerKey, PersistentDataType.BOOLEAN, true);
        resolveItemTexture(hammerMeta, modelData);
        hammerItem.setItemMeta(hammerMeta);

        this.hammerItem = hammerItem;

        Permission permission = new Permission("qbanhammers.hammers." + hammerItem, PermissionDefault.OP);
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        if (pluginManager.getPermission(permission.getName()) == null) {
            permission.addParent("qbanhammers.admin", true);
            pluginManager.addPermission(permission);
        }

        this.hammerKey = hammerKey;

        hammerList.add(this);
        plugin.getLogger().info("Created Hammer: " + hammerName);
    }

    private void resolveItemTexture(ItemMeta hammerMeta, String modelData) {


        if (modelData.startsWith("IA:")) {
            modelData = modelData.replace("IA:", "");
            CustomStack stack = CustomStack.getInstance(modelData);
            if (stack != null) {
                ItemStack iaStack = stack.getItemStack();
                try {
                    if (stack.getItemStack().getItemMeta().hasItemModel()) {
                        hammerMeta.setItemModel(stack.getItemStack().getItemMeta().getItemModel());
                    } else if (stack.getItemStack().getItemMeta().hasCustomModelData()) {
                        hammerMeta.setCustomModelData(stack.getItemStack().getItemMeta().getCustomModelData());
                        
                        iaStack.setItemMeta(plugin.getServer().getItemFactory().getItemMeta(iaStack.getType()));
                        hammerItem = iaStack;
                    }
                } catch (Exception exception) {
                    if (stack.getItemStack().getItemMeta().hasCustomModelData()) {

                        iaStack.setItemMeta(plugin.getServer().getItemFactory().getItemMeta(iaStack.getType()));
                        hammerItem = iaStack;
                    }
                }
            } else {
                plugin.getLogger().info(prefix + QBanHammers.getInstance().getConfig().getString("ModelData-Error", "&cInvalid model data for hammer {hammer}").replace("{hammer}", this.hammerName));
            }
        } else if (modelData.startsWith("Nexo:")) {
            modelData = modelData.replace("Nexo:", "");
            ItemBuilder nexoItem = NexoItems.itemFromId(modelData);
            if (nexoItem != null) {

                ItemStack nexoItemStack = nexoItem.build();

                try {
                    if (nexoItemStack.getItemMeta().hasItemModel()) {
                        hammerMeta.setItemModel(nexoItemStack.getItemMeta().getItemModel());
                    } else if (nexoItemStack.getItemMeta().hasCustomModelData()) {
                        hammerMeta.setCustomModelData(nexoItemStack.getItemMeta().getCustomModelData());
                        nexoItemStack.setItemMeta(plugin.getServer().getItemFactory().getItemMeta(nexoItemStack.getType()));
                        hammerItem = nexoItemStack;
                    }
                } catch (Exception exception) {
                    if (nexoItemStack.getItemMeta().hasCustomModelData()) {
                        hammerMeta.setCustomModelData(nexoItem.build().getItemMeta().getCustomModelData());
                        hammerItem = nexoItemStack;
                    }
                }


            } else {
                plugin.getLogger().info(prefix + QBanHammers.getInstance().getConfig().getString("ModelData-Error", "&cInvalid model data for hammer {hammer}").replace("{hammer}", this.hammerName));
            }
        } else {
            try {
                hammerMeta.setCustomModelData(Integer.parseInt(modelData));
            } catch (NumberFormatException e) {
                plugin.getLogger().info(prefix + QBanHammers.getInstance().getConfig().getString("ModelData-Error", "&cInvalid model data for hammer {hammer}").replace("{hammer}", this.hammerName));
            }
        }
    }


    public static List<Hammer> getHammerList() {
        return new ArrayList<>(hammerList);
    }

    public static void clearHammerList() {
        hammerList.clear();
    }

    public static Hammer getHammer(String hammerName) {
        Optional<Hammer> hammer = Hammer.getHammerList().stream().filter(option -> option.getHammerName().equalsIgnoreCase(hammerName)).findFirst();
        return hammer.orElse(null);
    }

    public static Hammer getHammer(ItemStack hammerItem) {
        Optional<Hammer> hammer = Hammer.getHammerList().stream().filter(option -> hammerItem.getItemMeta().getPersistentDataContainer().has(option.getHammerKey())).findFirst();
        return hammer.orElse(null);
    }

    public String getHammerName() {
        return hammerName;
    }

    public ItemStack getHammerItem() {
        return hammerItem;

    }

    public NamespacedKey getHammerKey() {
        return hammerKey;
    }

}
