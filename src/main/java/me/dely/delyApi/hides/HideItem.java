package me.dely.delyApi.hides;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class HideItem {

    private final String id;
    private String name;
    private List<String> lore;
    private Material material;
    private boolean glowing;

    public HideItem(String id, ConfigurationSection section) {
        this.id = id;
        this.name = section.getString("name", "Маска");
        this.material = Material.valueOf(section.getString("material", "BONE"));
        this.lore = section.getStringList("lore");
        this.glowing = section.getBoolean("glowing", false);
    }

    public void save(ConfigurationSection section) {
        section.set("name", name);
        section.set("material", material.name());
        section.set("lore", lore);
        section.set("glowing", glowing);
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        meta.setLore(lore);
        if (glowing) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<String> getLore() { return lore; }
    public Material getMaterial() { return material; }
    public boolean isGlowing() { return glowing; }

    public void setName(String name) { this.name = name; }
    public void setLore(List<String> lore) { this.lore = lore; }
    public void setMaterial(Material material) { this.material = material; }
    public void setGlowing(boolean glowing) { this.glowing = glowing; }
}
