package me.dely.delyApi.listeners;

import me.dely.delyApi.DelyApi;
import me.dely.delyApi.loot.LootContainer;
import me.dely.delyApi.menu.MenuHolder;
import me.dely.delyApi.menu.loot.LootMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class LootMenuListener implements Listener {

    private final DelyApi plugin;

    public LootMenuListener(DelyApi plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!(e.getInventory().getHolder() instanceof MenuHolder holder)) return;

        if (holder.getMenuId().equals(LootMenu.MENU_CHANCE)) {
            ItemStack item = e.getCurrentItem();
            if (item == null || item.getItemMeta() == null) return;
            e.setCancelled(true);
            int change = e.getClick().isShiftClick() ? 10 : 1;
            if (e.getClick().isRightClick()) change = -change;
            plugin.getLootController().changeChanceInRealTime(e.getInventory(), e.getSlot(), change);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        if (!(e.getInventory().getHolder() instanceof MenuHolder holder)) return;

        String menuId = holder.getMenuId();
        if (!menuId.equals(LootMenu.MENU_EDITOR) && !menuId.equals(LootMenu.MENU_CHANCE)) return;

        LootContainer loot = holder.getLootContainer();
        if (loot != null) plugin.getLootController().saveLoot(e.getInventory(), loot);

        Runnable callback = LootMenu.pollCallback(player.getUniqueId());
        if (callback != null) Bukkit.getScheduler().runTask(plugin, callback);
    }
}
