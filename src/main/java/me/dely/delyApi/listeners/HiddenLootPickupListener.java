package me.dely.delyApi.listeners;

import me.dely.delyApi.api.events.HiddenLootPickupEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import java.util.Base64;
import java.util.HashMap;

public class HiddenLootPickupListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        Item itemEntity = e.getItem();
        if (!itemEntity.hasMetadata("dely_hidden_loot")) return;

        String base64 = null;
        for (MetadataValue mv : itemEntity.getMetadata("dely_real_item_nbt")) {
            base64 = mv.asString();
            break;
        }
        if (base64 == null || base64.isEmpty()) return;

        ItemStack realItem = deserializeItem(base64);
        if (realItem == null) return;

        HiddenLootPickupEvent apiEvent = new HiddenLootPickupEvent(player, itemEntity, realItem);
        Bukkit.getPluginManager().callEvent(apiEvent);
        if (apiEvent.isCancelled()) return;

        e.setCancelled(true);
        itemEntity.remove();

        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(apiEvent.getRealItem());
        leftover.values().forEach(item ->
                player.getWorld().dropItemNaturally(player.getLocation(), item)
        );

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
    }

    private ItemStack deserializeItem(String base64) {
        try {
            byte[] data = Base64.getDecoder().decode(base64);
            return ItemStack.deserializeBytes(data);
        } catch (Exception ex) {
            return null;
        }
    }
}
