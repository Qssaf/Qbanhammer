package me.Qssaf.QBanHammers;

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

import static me.Qssaf.QBanHammers.ConfigManager.prefix;
import static me.Qssaf.QBanHammers.ConfigManager.text;

public class Hammer {
    private static final List<Hammer> hammerList = new ArrayList<>();


    private ItemStack hammerItem;
    private String hammerName;
    private NamespacedKey hammerKey;

    public Hammer(Hammer hammer) {
        this.hammerItem = hammer.getHammerItem().clone();
        this.hammerName = hammer.getHammerName();
        this.hammerKey = hammer.getHammerKey();

    }

    public Hammer(String HammerName) {
        QBanHammers plugin = QBanHammers.getInstance();
        if (!plugin.getConfig().contains("hammers." + HammerName)) {
            plugin.getLogger().severe("Unable to load Hammer: " + HammerName);
            plugin.getLogger().severe("Reason: Couldn't find {hammer} in the config".replace("{hammer}", HammerName));
            return;
        }
        this.hammerName = HammerName;

        ItemStack hammerItem = new ItemStack(Material.MACE);
        ItemMeta hammerMeta = hammerItem.getItemMeta();
        hammerMeta.displayName(text(QBanHammers.getInstance().getConfig().getString("hammers." + HammerName + ".name")));
        List<Component> lore = new ArrayList<>();
        for (String line : QBanHammers.getInstance().getConfig().getStringList("hammers." + HammerName + ".lore")) {
            lore.add(text(line));
        }

        hammerMeta.lore(lore);
        hammerMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        hammerMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        String modelData = QBanHammers.getInstance().getConfig().getString("hammers." + HammerName + ".model-data", "0");

        if (modelData.startsWith("ia:")) {
            modelData = modelData.replace("ia:", "");
            CustomStack stack = CustomStack.getInstance(modelData);
            if (stack != null) {
                hammerMeta.setCustomModelData(stack.getItemStack().getItemMeta().getCustomModelData());
                hammerItem = stack.getItemStack();
            } else {
                plugin.getLogger().info(prefix + QBanHammers.getInstance().getConfig().getString("ModelData-Error", "&cInvalid model data for hammer {hammer}").replace("{hammer}", HammerName));
                return;
            }
        } else if (modelData.startsWith("Nexo:")) {
            modelData = modelData.replace("Nexo:", "");
            ItemBuilder nexoItem = NexoItems.itemFromId(modelData);
            if (nexoItem != null) {
                hammerMeta.setCustomModelData(nexoItem.build().getItemMeta().getCustomModelData());
                hammerItem = nexoItem.build();

            } else {
                plugin.getLogger().info(prefix + QBanHammers.getInstance().getConfig().getString("ModelData-Error", "&cInvalid model data for hammer {hammer}").replace("{hammer}", HammerName));
                return;
            }
        } else {
            try {
                hammerMeta.setCustomModelData(Integer.parseInt(modelData));
            } catch (NumberFormatException e) {
                plugin.getLogger().info(prefix + QBanHammers.getInstance().getConfig().getString("ModelData-Error", "&cInvalid model data for hammer {hammer}").replace("{hammer}", HammerName));
                return;
            }
        }

        hammerMeta.setUnbreakable(true);
        hammerMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        NamespacedKey hammerKey = new NamespacedKey(plugin, this.hammerName);
        hammerMeta.getPersistentDataContainer().set(hammerKey, PersistentDataType.BOOLEAN, true);
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
        plugin.getLogger().info("Created Hammer: " + HammerName);
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
