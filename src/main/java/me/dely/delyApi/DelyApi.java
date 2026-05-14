package me.dely.delyApi;

import me.dely.delyApi.hides.HideManager;
import me.dely.delyApi.listeners.ApiChatListener;
import me.dely.delyApi.listeners.HiddenLootPickupListener;
import me.dely.delyApi.listeners.HidesMenuListener;
import me.dely.delyApi.listeners.LootMenuListener;
import me.dely.delyApi.loot.LootController;
import me.dely.delyApi.menu.hides.HidesMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class DelyApi extends JavaPlugin {

    private static DelyApi instance;

    private HideManager hideManager;
    private LootController lootController;

    @Override
    public void onEnable() {
        instance = this;

        getDataFolder().mkdirs();
        this.hideManager = new HideManager(new File(getDataFolder(), "hides.yml"));
        this.lootController = new LootController(this);

        Bukkit.getPluginManager().registerEvents(new HiddenLootPickupListener(), this);
        Bukkit.getPluginManager().registerEvents(new HidesMenuListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ApiChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new LootMenuListener(this), this);


        getLogger().info("DelyApi включён!");
    }

    @Override
    public void onDisable() {
        if (hideManager != null) hideManager.saveAll();
        getLogger().info("DelyApi выключён!");
    }

    public static DelyApi getInstance() { return instance; }

    public HideManager getHideManager() { return hideManager; }

    public LootController getLootController() { return lootController; }
}
