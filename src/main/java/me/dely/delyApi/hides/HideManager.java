package me.dely.delyApi.hides;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HideManager {

    private final Map<String, HideItem> hides = new HashMap<>();
    private final File file;
    private FileConfiguration cfg;

    public HideManager(File hidesFile) {
        this.file = hidesFile;

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.cfg = YamlConfiguration.loadConfiguration(file);
        load();
    }

    private void load() {
        hides.clear();

        ConfigurationSection section = cfg.getConfigurationSection("hides");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            ConfigurationSection hideSec = section.getConfigurationSection(id);
            if (hideSec == null) continue;
            hides.put(id.toLowerCase(), new HideItem(id, hideSec));
        }
    }

    public void saveAll() {
        cfg.set("hides", null);

        ConfigurationSection section = cfg.createSection("hides");
        for (HideItem item : hides.values()) {
            ConfigurationSection hideSec = section.createSection(item.getId());
            hideSec.set("name", item.getName());
            hideSec.set("material", item.getMaterial().name());
            hideSec.set("lore", item.getLore());
            hideSec.set("glowing", item.isGlowing());
        }

        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HideItem getHide(String id) {
        if (id == null) return null;
        return hides.get(id.toLowerCase());
    }

    public List<HideItem> getHides() {
        return new ArrayList<>(hides.values());
    }

    public HideItem createHide(String id) {
        String low = id.toLowerCase();
        if (hides.containsKey(low)) return null;

        ConfigurationSection section = cfg.createSection("hides." + id);
        section.set("name", "Новая маска " + id);
        section.set("material", "BONE");
        section.set("lore", List.of("§7Скрывает предметы"));
        section.set("glowing", false);

        HideItem item = new HideItem(id, section);
        hides.put(low, item);
        saveAll();
        return item;
    }

    public boolean deleteHide(String id) {
        String low = id.toLowerCase();
        if (!hides.containsKey(low)) return false;

        hides.remove(low);
        cfg.set("hides." + id, null);
        saveAll();
        return true;
    }
}
