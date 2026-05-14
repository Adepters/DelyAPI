package me.dely.delyApi.menu.hides;

import me.dely.delyApi.DelyApi;
import me.dely.delyApi.hides.HideItem;
import me.dely.delyApi.hides.HideManager;
import me.dely.delyApi.menu.MenuHolder;
import me.dely.delyApi.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class HideMaterialMenu {

    private static final NamespacedKey MATERIAL_KEY =
            new NamespacedKey(DelyApi.getInstance(), "hide_material_select");

    private static final Material[] SUGGESTED = {
            Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD,
            Material.BOW, Material.CROSSBOW, Material.TRIDENT,
            Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE,
            Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE,
            Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.DIAMOND_HELMET,
            Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE,
            Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS,
            Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS,
            Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET,
            Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT,
            Material.IRON_INGOT, Material.NETHERITE_INGOT,
            Material.GOLD_NUGGET, Material.IRON_NUGGET,
            Material.BONE, Material.COAL, Material.CHARCOAL,
            Material.APPLE, Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE,
            Material.BREAD, Material.COOKED_BEEF, Material.COOKED_PORKCHOP,
            Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION,
            Material.EXPERIENCE_BOTTLE, Material.BLAZE_ROD, Material.ENDER_PEARL,
            Material.NETHER_STAR, Material.HEART_OF_THE_SEA,
            Material.BOOK, Material.ENCHANTED_BOOK, Material.MAP,
            Material.COMPASS, Material.CLOCK,
            Material.FISHING_ROD, Material.FLINT_AND_STEEL, Material.LEAD,
            Material.SADDLE, Material.NAME_TAG, Material.TOTEM_OF_UNDYING,
            Material.FEATHER, Material.STRING, Material.ARROW,
            Material.SNOWBALL, Material.EGG, Material.FIREWORK_ROCKET
    };

    public static void open(Player player, HideItem hide, HideManager hideManager) {
        String title = ColorUtil.colorize("#323232Выбор материала: " + hide.getId());
        Inventory inv = Bukkit.createInventory(
                new MenuHolder("DELY_HIDE_MATERIAL_SELECT", hide.getId()).withHideManager(hideManager),
                54,
                title
        );

        for (int i = 0; i < SUGGESTED.length && i < 54; i++) {
            Material mat = SUGGESTED[i];
            boolean selected = mat == hide.getMaterial();
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            String name = mat.name().replace("_", " ");
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
            meta.setDisplayName(ColorUtil.colorize("#F4C542" + name));

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ColorUtil.colorize(selected ? "#a8ff78● Выбрано" : "#757575ЛКМ - выбрать"));
            lore.add(ColorUtil.colorize("#F4C542ПКМ #757575- вписать material id"));
            meta.setLore(lore);

            meta.getPersistentDataContainer().set(MATERIAL_KEY, PersistentDataType.STRING, mat.name());
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        player.openInventory(inv);
    }

    public static NamespacedKey getMaterialKey() { return MATERIAL_KEY; }
}
