package me.dely.delyApi.api.events;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class HiddenLootPickupEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Item itemEntity;
    private ItemStack realItem;
    private boolean cancelled = false;

    public HiddenLootPickupEvent(Player player, Item itemEntity, ItemStack realItem) {
        this.player = player;
        this.itemEntity = itemEntity;
        this.realItem = realItem;
    }

    public Player getPlayer() { return player; }
    public Item getItemEntity() { return itemEntity; }
    public ItemStack getRealItem() { return realItem; }
    public void setRealItem(ItemStack realItem) { this.realItem = realItem; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
