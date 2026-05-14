package me.dely.delyApi.listeners;

import me.dely.delyApi.DelyApi;
import me.dely.delyApi.hides.HideItem;
import me.dely.delyApi.hides.HideManager;
import me.dely.delyApi.input.ApiInputHandler;
import me.dely.delyApi.menu.MenuHolder;
import me.dely.delyApi.menu.hides.HideEditMenu;
import me.dely.delyApi.menu.hides.HideMaterialMenu;
import me.dely.delyApi.menu.hides.HidesMenu;
import me.dely.delyApi.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class HidesMenuListener implements Listener {

    private final DelyApi plugin;

    public HidesMenuListener(DelyApi plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!(e.getInventory().getHolder() instanceof MenuHolder holder)) return;

        HideManager hm = holder.getHideManager();
        if (hm == null) return;

        switch (holder.getMenuId()) {
            case "DELY_HIDES_MENU"          -> handleHidesMenuClick(e, player, hm);
            case "DELY_HIDE_EDIT_MENU"      -> handleHideEditClick(e, player, hm);
            case "DELY_HIDE_MATERIAL_SELECT" -> handleHideMaterialClick(e, player, hm);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        if (!(e.getInventory().getHolder() instanceof MenuHolder holder)) return;

        UUID uuid = player.getUniqueId();
        String menuId = holder.getMenuId();
        HideManager hm = holder.getHideManager();
        if (hm == null) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder) return;
            if (ApiInputHandler.isAwaiting(uuid)) return;

            switch (menuId) {
                case "DELY_HIDE_EDIT_MENU", "DELY_HIDE_MATERIAL_SELECT" -> {
                    hm.saveAll();
                    HidesMenu.open(player, hm);
                }
            }
        });
    }

    private void handleHidesMenuClick(InventoryClickEvent e, Player player, HideManager hm) {
        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getItemMeta() == null) return;

        String action = item.getItemMeta().getPersistentDataContainer()
                .get(HidesMenu.getActionKey(), PersistentDataType.STRING);
        if (action == null) return;

        switch (action) {
            case "CREATE_HIDE" -> {
                player.closeInventory();
                player.sendMessage(ColorUtil.colorize("#F4C542▶ Введите ID новой маскировки (только латиница):"));
                ApiInputHandler.add(player.getUniqueId(), ApiInputHandler.InputType.CREATE_HIDE, hm);
            }
            case "OPEN_HIDE" -> {
                String id = item.getItemMeta().getPersistentDataContainer()
                        .get(HidesMenu.getHideIdKey(), PersistentDataType.STRING);
                if (id == null) return;

                if (e.isRightClick() || e.getClick() == ClickType.SHIFT_LEFT) {
                    hm.deleteHide(id);
                    HidesMenu.open(player, hm);
                } else {
                    HideItem hide = hm.getHide(id);
                    if (hide != null) HideEditMenu.open(player, hide, hm);
                }
            }
        }
    }

    private void handleHideEditClick(InventoryClickEvent e, Player player, HideManager hm) {
        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getItemMeta() == null) return;

        String hideId = ((MenuHolder) e.getInventory().getHolder()).getItemId();
        HideItem hide = hm.getHide(hideId);
        if (hide == null) return;

        String action = item.getItemMeta().getPersistentDataContainer()
                .get(HideEditMenu.getActionKey(), PersistentDataType.STRING);
        if (action == null) return;

        switch (action) {
            case "EDIT_NAME" -> {
                player.closeInventory();
                player.sendMessage(ColorUtil.colorize("#F4C542Введите новое имя маскировки:"));
                ApiInputHandler.add(player.getUniqueId(), ApiInputHandler.InputType.HIDE_NAME, hideId, hm);
            }
            case "EDIT_MATERIAL" -> {
                if (e.isRightClick() || e.getClick() == ClickType.SHIFT_LEFT) {
                    player.closeInventory();
                    player.sendMessage(ColorUtil.colorize("#F4C542Введите material id для маскировки."));
                    player.sendMessage(ColorUtil.colorize("#757575Пример: DIAMOND или NETHER_STAR"));
                    ApiInputHandler.add(player.getUniqueId(), ApiInputHandler.InputType.HIDE_MATERIAL, hideId, hm);
                } else {
                    HideMaterialMenu.open(player, hide, hm);
                }
            }
            case "EDIT_LORE" -> {
                player.closeInventory();
                player.sendMessage(ColorUtil.colorize("#757575Одна строка = одно сообщение."));
                player.sendMessage(ColorUtil.colorize("#757575finish - закончить, clear - очистить, del - удалить последнюю строку."));
                ApiInputHandler.add(player.getUniqueId(), ApiInputHandler.InputType.HIDE_LORE, hideId, hm);
            }
            case "TOGGLE_GLOW" -> {
                hide.setGlowing(!hide.isGlowing());
                hm.saveAll();
                HideEditMenu.open(player, hide, hm);
            }
        }
    }

    private void handleHideMaterialClick(InventoryClickEvent e, Player player, HideManager hm) {
        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getItemMeta() == null) return;

        String hideId = ((MenuHolder) e.getInventory().getHolder()).getItemId();
        HideItem hide = hm.getHide(hideId);
        if (hide == null) return;

        if (e.isRightClick() || e.getClick() == ClickType.SHIFT_LEFT) {
            player.closeInventory();
            player.sendMessage(ColorUtil.colorize("#F4C542Введите material id для маскировки."));
            player.sendMessage(ColorUtil.colorize("#757575Пример: DIAMOND или NETHER_STAR"));
            ApiInputHandler.add(player.getUniqueId(), ApiInputHandler.InputType.HIDE_MATERIAL, hideId, hm);
            return;
        }

        String matName = item.getItemMeta().getPersistentDataContainer()
                .get(HideMaterialMenu.getMaterialKey(), PersistentDataType.STRING);
        if (matName == null) return;

        try {
            hide.setMaterial(Material.valueOf(matName));
            hm.saveAll();
            HideEditMenu.open(player, hide, hm);
        } catch (IllegalArgumentException ignored) {}
    }
}
