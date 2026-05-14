package me.dely.delyApi.menu;

import me.dely.delyApi.hides.HideManager;
import me.dely.delyApi.loot.LootContainer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuHolder implements InventoryHolder {

    private final String menuId;
    private final String itemId;
    private HideManager hideManager;
    private LootContainer lootContainer;

    public MenuHolder(String menuId, String itemId) {
        this.menuId = menuId;
        this.itemId = itemId;
    }

    public MenuHolder withHideManager(HideManager hideManager) {
        this.hideManager = hideManager;
        return this;
    }

    public MenuHolder withLootContainer(LootContainer lootContainer) {
        this.lootContainer = lootContainer;
        return this;
    }

    @Override
    public Inventory getInventory() { return null; }

    public String getMenuId() { return menuId; }
    public String getItemId() { return itemId; }
    public HideManager getHideManager() { return hideManager; }
    public LootContainer getLootContainer() { return lootContainer; }
}
