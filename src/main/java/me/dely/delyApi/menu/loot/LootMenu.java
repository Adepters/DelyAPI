package me.dely.delyApi.menu.loot;

import me.dely.delyApi.DelyApi;
import me.dely.delyApi.loot.LootContainer;
import me.dely.delyApi.menu.MenuHolder;
import me.dely.delyApi.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LootMenu {

    public static final String MENU_EDITOR = "DELY_LOOT_EDITOR";
    public static final String MENU_CHANCE = "DELY_CHANCE_EDITOR";

    private static final Map<UUID, Runnable> closeCallbacks = new HashMap<>();

    public static void openEditor(Player player, LootContainer loot) {
        openEditor(player, loot, null);
    }

    public static void openEditor(Player player, LootContainer loot, Runnable onClose) {
        setCallback(player.getUniqueId(), onClose);
        Inventory inv = Bukkit.createInventory(
                new MenuHolder(MENU_EDITOR, "").withLootContainer(loot),
                54,
                ColorUtil.colorize("#323232Лут:"));
        DelyApi.getInstance().getLootController().loadLoot(inv, loot, false);
        player.openInventory(inv);
    }

    public static void openChanceEditor(Player player, LootContainer loot) {
        openChanceEditor(player, loot, null);
    }

    public static void openChanceEditor(Player player, LootContainer loot, Runnable onClose) {
        setCallback(player.getUniqueId(), onClose);
        Inventory inv = Bukkit.createInventory(
                new MenuHolder(MENU_CHANCE, "").withLootContainer(loot),
                54,
                ColorUtil.colorize("#323232Редактор шансов:"));
        DelyApi.getInstance().getLootController().loadLoot(inv, loot, true);
        player.openInventory(inv);
    }

    private static void setCallback(UUID uuid, Runnable callback) {
        if (callback != null) closeCallbacks.put(uuid, callback);
        else closeCallbacks.remove(uuid);
    }

    public static Runnable pollCallback(UUID uuid) {
        return closeCallbacks.remove(uuid);
    }
}
