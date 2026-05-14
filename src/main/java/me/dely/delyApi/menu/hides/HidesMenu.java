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

public class HidesMenu {

    private static final NamespacedKey ACTION_KEY =
            new NamespacedKey(DelyApi.getInstance(), "hides_action");

    private static final NamespacedKey HIDE_ID_KEY =
            new NamespacedKey(DelyApi.getInstance(), "hide_id");

    public static void open(Player player, HideManager hideManager) {
        List<HideItem> hides = hideManager.getHides();

        int size = Math.max(27, (int) Math.ceil((hides.size() + 1) / 9.0) * 9);
        size = Math.min(size, 54);

        String title = ColorUtil.colorize("#323232Маскировки");
        Inventory inv = Bukkit.createInventory(
                new MenuHolder("DELY_HIDES_MENU", "").withHideManager(hideManager),
                size,
                title
        );

        ItemStack createItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta createMeta = createItem.getItemMeta();
        if (createMeta != null) {
            createMeta.setDisplayName(ColorUtil.colorize("#a8ff78+ Создать маскировку"));
            createMeta.setLore(List.of(
                    ColorUtil.colorize(""),
                    ColorUtil.colorize("#757575Нажмите, чтобы создать"),
                    ColorUtil.colorize("#757575новый предмет маскировки"),
                    ColorUtil.colorize("")
            ));
            createMeta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, "CREATE_HIDE");
            createItem.setItemMeta(createMeta);
        }
        inv.setItem(size - 1, createItem);

        int slot = 0;
        for (HideItem hide : hides) {
            if (slot >= size - 1) break;

            ItemStack item = new ItemStack(hide.getMaterial());
            ItemMeta meta = item.getItemMeta();
            if (meta == null) { slot++; continue; }

            meta.setDisplayName(ColorUtil.colorize("#F4C542" + hide.getName()));

            List<String> lore = new ArrayList<>();
            lore.add(ColorUtil.colorize("#757575ID: #ffffff" + hide.getId()));

            if (hide.getLore() != null && !hide.getLore().isEmpty()) {
                lore.add("");
                for (String line : hide.getLore()) lore.add(ColorUtil.colorize(line));
            }

            lore.add("");
            lore.add(ColorUtil.colorize("#F4C542ЛКМ #757575- настроить"));
            lore.add(ColorUtil.colorize("#FF4E5EПКМ #757575- удалить"));

            meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, "OPEN_HIDE");
            meta.getPersistentDataContainer().set(HIDE_ID_KEY, PersistentDataType.STRING, hide.getId());
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        player.openInventory(inv);
    }

    public static NamespacedKey getActionKey() { return ACTION_KEY; }
    public static NamespacedKey getHideIdKey() { return HIDE_ID_KEY; }
}
