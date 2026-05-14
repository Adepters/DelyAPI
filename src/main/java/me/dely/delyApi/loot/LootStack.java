package me.dely.delyApi.loot;

import me.dely.delyApi.hides.HideItem;
import org.bukkit.inventory.ItemStack;

public class LootStack {

    private final ItemStack realItem;
    private final HideItem hideItem;

    public LootStack(ItemStack realItem, HideItem hideItem) {
        this.realItem = realItem.clone();
        this.hideItem = hideItem;
    }

    public ItemStack getItem() {
        return realItem.clone();
    }

    public ItemStack getDisplayItem() {
        return hideItem.getItem();
    }

    public HideItem getHideItem() {
        return hideItem;
    }
}
