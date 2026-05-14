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

public class HideEditMenu {

    private static final NamespacedKey ACTION_KEY =
            new NamespacedKey(DelyApi.getInstance(), "hide_edit_action");

    public static void open(Player player, HideItem hide, HideManager hideManager) {
        String title = ColorUtil.colorize("#323232Редактирование: " + hide.getId());
        Inventory inv = Bukkit.createInventory(
                new MenuHolder("DELY_HIDE_EDIT_MENU", hide.getId()).withHideManager(hideManager),
                27,
                title
        );

        setItem(inv, 10, Material.NAME_TAG, "EDIT_NAME",
                "#F4C542Имя",
                "#757575Текущее: #ffffff" + hide.getName(),
                "",
                "#F4C542Клик #757575- изменить");

        setItem(inv, 12, hide.getMaterial(), "EDIT_MATERIAL",
                "#F4C542Материал",
                "#757575Текущий: #ffffff" + hide.getMaterial().name(),
                "",
                "#a8ff78ЛКМ #757575- выбрать из списка",
                "#F4C542ПКМ #757575- вписать material id");

        List<String> loreLore = new ArrayList<>();
        loreLore.add("#757575Текущий lore:");
        if (hide.getLore() == null || hide.getLore().isEmpty()) loreLore.add("#ffffff(пусто)");
        else for (String line : hide.getLore()) loreLore.add("#ffffff" + line);
        loreLore.add("");
        loreLore.add("#F4C542Клик #757575- редактировать");
        setItem(inv, 14, Material.BOOK, "EDIT_LORE", "#F4C542Описание", loreLore.toArray(new String[0]));

        setItem(inv, 16,
                hide.isGlowing() ? Material.GLOWSTONE_DUST : Material.GUNPOWDER,
                "TOGGLE_GLOW",
                "#F4C542Свечение",
                "#757575Сейчас: #ffffff" + (hide.isGlowing() ? "включено" : "выключено"),
                "",
                "#F4C542Клик #757575- переключить");

        player.openInventory(inv);
    }

    private static void setItem(Inventory inv, int slot, Material mat, String action, String name, String... loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.setDisplayName(ColorUtil.colorize(name));
        meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, action);
        List<String> lore = new ArrayList<>();
        for (String line : loreLines) lore.add(ColorUtil.colorize(line));
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    public static NamespacedKey getActionKey() { return ACTION_KEY; }
}
