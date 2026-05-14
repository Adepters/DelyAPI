package me.dely.delyApi.loot;

import me.dely.delyApi.hides.HideItem;
import me.dely.delyApi.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class LootController {

    private final NamespacedKey chanceKey;
    private final NamespacedKey uniqueIdKey;
    private final JavaPlugin plugin;

    public LootController(JavaPlugin plugin) {
        this.plugin = plugin;
        this.chanceKey = new NamespacedKey(plugin, "loot_chance");
        this.uniqueIdKey = new NamespacedKey(plugin, "unique_id");
    }

    public void saveLoot(Inventory inventory, LootContainer loot) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(loot.getLootFile());
        List<Map<String, Object>> items = new ArrayList<>();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType().isAir()) continue;

            ItemStack clone = item.clone();
            ItemMeta meta = clone.getItemMeta();
            double chance = 100.0;

            if (meta != null) {
                if (meta.getPersistentDataContainer().has(chanceKey, PersistentDataType.DOUBLE)) {
                    chance = meta.getPersistentDataContainer().get(chanceKey, PersistentDataType.DOUBLE);
                }
                List<String> lore = meta.getLore();
                if (lore != null) {
                    lore = new ArrayList<>(lore);
                    lore.removeIf(line -> line.contains("Текущий шанс") || line.contains("SHIFT") || line.contains("ЛКМ") || line.contains("ПКМ"));
                    meta.setLore(lore);
                    clone.setItemMeta(meta);
                }
            }

            String encoded = encodeItem(clone);
            if (encoded == null) continue;

            Map<String, Object> itemData = new HashMap<>();
            itemData.put("item", encoded);
            itemData.put("chance", chance);
            items.add(itemData);
        }

        config.set("items", items);
        try {
            config.save(loot.getLootFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadLoot(Inventory inventory, LootContainer loot, boolean isChanceEditor) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(loot.getLootFile());
        inventory.clear();

        if (!config.contains("items")) return;

        List<Map<?, ?>> items = config.getMapList("items");
        int slot = 0;

        for (Map<?, ?> map : items) {
            if (slot >= inventory.getSize()) break;

            String encoded = (String) map.get("item");
            ItemStack item = decodeItem(encoded);
            if (item == null) continue;

            double chance = map.containsKey("chance") ? ((Number) map.get("chance")).doubleValue() : 100.0;

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(chanceKey, PersistentDataType.DOUBLE, chance);
                if (isChanceEditor) applyChanceLore(meta, chance);
                item.setItemMeta(meta);
            }

            inventory.setItem(slot++, item);
        }
    }

    public ItemStack[] getRandomLoot(LootContainer loot) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(loot.getLootFile());
        List<Map<?, ?>> items = config.getMapList("items");

        if (items.isEmpty()) return null;

        Random r = new Random();
        int amount = r.nextInt((loot.getMaxLoot() - loot.getMinLoot()) + 1) + loot.getMinLoot();
        ItemStack[] result = new ItemStack[amount];

        for (int i = 0; i < amount; i++) {
            Map<?, ?> selected = rollByChance(items);
            if (selected != null) {
                ItemStack item = decodeItem((String) selected.get("item"));
                if (item != null) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.getPersistentDataContainer().remove(chanceKey);
                        item.setItemMeta(meta);
                    }
                    result[i] = item;
                }
            }
        }
        return result;
    }

    public void changeChanceInRealTime(Inventory inventory, int slot, int delta) {
        ItemStack item = inventory.getItem(slot);
        if (item == null || item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        double chance = 100;
        if (meta.getPersistentDataContainer().has(chanceKey, PersistentDataType.DOUBLE)) {
            chance = meta.getPersistentDataContainer().get(chanceKey, PersistentDataType.DOUBLE);
        }

        chance = Math.max(0, Math.min(100, chance + delta));

        meta.getPersistentDataContainer().set(chanceKey, PersistentDataType.DOUBLE, chance);
        applyChanceLore(meta, chance);
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }

    public List<Item> dropLoot(Location location, LootContainer loot, List<HideItem> hides, @Nullable Player player) {
        ItemStack[] lootItems = getRandomLoot(loot);
        if (lootItems == null) return Collections.emptyList();

        List<HideItem> hideList = (hides != null && !hides.isEmpty()) ? hides : Collections.emptyList();
        List<Item> dropped = new ArrayList<>();
        Random rand = new Random();

        for (ItemStack rawItem : lootItems) {
            if (rawItem == null) continue;
            ItemStack realItem = stripLootMeta(rawItem);

            if (!hideList.isEmpty()) {
                HideItem hideItem = hideList.get(rand.nextInt(hideList.size()));
                LootStack lootStack = new LootStack(realItem, hideItem);
                ItemStack display = makeUnique(lootStack.getDisplayItem());

                Item droppedItem = location.getWorld().dropItemNaturally(location, display);
                droppedItem.setMetadata("dely_hidden_loot",
                        new org.bukkit.metadata.FixedMetadataValue(plugin, lootStack.getItem().serialize().toString()));
                droppedItem.setMetadata("dely_real_item_nbt",
                        new org.bukkit.metadata.FixedMetadataValue(plugin, serializeItem(realItem)));
                droppedItem.setGlowing(true);
                makeGlow(droppedItem);
                dropped.add(droppedItem);

            } else if (player != null) {
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(realItem);
                leftover.values().forEach(left -> {
                    Item droppedItem = location.getWorld().dropItemNaturally(location, left);
                    dropped.add(droppedItem);
                });

            } else {
                Item droppedItem = location.getWorld().dropItemNaturally(location, makeUnique(realItem));
                dropped.add(droppedItem);
            }
        }
        return dropped;
    }

    public String encodeItem(ItemStack item) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             BukkitObjectOutputStream data = new BukkitObjectOutputStream(output)) {
            data.writeObject(item);
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    public ItemStack decodeItem(String base64) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
             BukkitObjectInputStream data = new BukkitObjectInputStream(input)) {
            return (ItemStack) data.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    public void makeGlow(Item item) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam("dely_glow_items");
        if (team == null) {
            team = board.registerNewTeam("dely_glow_items");
            team.setColor(ChatColor.YELLOW);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
        team.addEntry(item.getUniqueId().toString());
    }

    private Map<?, ?> rollByChance(List<Map<?, ?>> items) {
        double total = 0;
        for (Map<?, ?> map : items) total += ((Number) map.get("chance")).doubleValue();

        double random = Math.random() * total;
        double current = 0;
        for (Map<?, ?> map : items) {
            current += ((Number) map.get("chance")).doubleValue();
            if (random <= current) return map;
        }
        return null;
    }

    private void applyChanceLore(ItemMeta meta, double chance) {
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(line -> line.contains("Текущий шанс") || line.contains("SHIFT") || line.contains("ЛКМ") || line.contains("ПКМ"));
        lore.add(ColorUtil.colorize("#F4C542Текущий шанс: #9FE2FF" + chance));
        lore.add(ColorUtil.colorize("#9FE2FFЛКМ #757575→ #F4C542+1%"));
        lore.add(ColorUtil.colorize("#9FE2FFПКМ #757575→ #F4C542-1%"));
        lore.add(ColorUtil.colorize("#9FE2FFSHIFT + клик #757575→ #F4C542изменить на 10%"));
        meta.setLore(lore);
    }

    private ItemStack stripLootMeta(ItemStack item) {
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) return clone;
        meta.getPersistentDataContainer().remove(chanceKey);
        List<String> lore = meta.getLore();
        if (lore != null) {
            lore = new ArrayList<>(lore);
            lore.removeIf(line -> line.contains("Текущий шанс") || line.contains("SHIFT") || line.contains("ЛКМ") || line.contains("ПКМ"));
            meta.setLore(lore.isEmpty() ? null : lore);
        }
        clone.setItemMeta(meta);
        return clone;
    }

    private ItemStack makeUnique(ItemStack item) {
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) return clone;
        meta.getPersistentDataContainer().set(uniqueIdKey, PersistentDataType.STRING, UUID.randomUUID().toString());
        clone.setItemMeta(meta);
        return clone;
    }

    private String serializeItem(ItemStack item) {
        try {
            return Base64.getEncoder().encodeToString(item.serializeAsBytes());
        } catch (Exception e) {
            return "";
        }
    }

    public NamespacedKey getChanceKey() {
        return chanceKey;
    }
}
