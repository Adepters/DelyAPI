package me.dely.delyApi.listeners;

import me.dely.delyApi.DelyApi;
import me.dely.delyApi.hides.HideItem;
import me.dely.delyApi.hides.HideManager;
import me.dely.delyApi.input.ApiInputHandler;
import me.dely.delyApi.menu.hides.HideEditMenu;
import me.dely.delyApi.menu.hides.HidesMenu;
import me.dely.delyApi.utils.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ApiChatListener implements Listener {

    private final DelyApi plugin;

    public ApiChatListener(DelyApi plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!ApiInputHandler.isAwaiting(uuid)) return;
        e.setCancelled(true);

        String message = e.getMessage().trim();
        if (message.isEmpty()) return;

        ApiInputHandler.InputData data = ApiInputHandler.getData(uuid);
        if (data == null) return;

        HideManager hm = data.hideManager();
        if (hm == null) return;

        if (message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("отмена")) {
            player.sendMessage(ColorUtil.colorize("#FF4E5E✗ Ввод отменён."));
            ApiInputHandler.remove(uuid);
            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> HidesMenu.open(player, hm));
            return;
        }

        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> handleInput(player, uuid, data, message, hm));
    }

    private void handleInput(Player player, UUID uuid, ApiInputHandler.InputData data, String message, HideManager hm) {
        switch (data.type()) {
            case CREATE_HIDE -> {
                if (!message.matches("^[a-zA-Z0-9_]+$")) {
                    player.sendMessage(ColorUtil.colorize("#FF4E5E⚠ Только латиница и цифры!"));
                    return;
                }
                HideItem hide = hm.createHide(message);
                if (hide == null) {
                    player.sendMessage(ColorUtil.colorize("#FF4E5E⚠ Маскировка с таким ID уже существует!"));
                    return;
                }
                player.sendMessage(ColorUtil.colorize("#a8ff78✔ Маскировка создана: " + message));
                ApiInputHandler.remove(uuid);
                HideEditMenu.open(player, hide, hm);
            }
            case HIDE_NAME -> {
                String context = data.context();
                if (context == null) { ApiInputHandler.remove(uuid); return; }
                HideItem hide = hm.getHide(context);
                if (hide == null) { ApiInputHandler.remove(uuid); return; }
                hide.setName(ColorUtil.colorize(message));
                hm.saveAll();
                player.sendMessage(ColorUtil.colorize("#a8ff78✔ Имя: " + ColorUtil.colorize(message)));
                ApiInputHandler.remove(uuid);
                HideEditMenu.open(player, hide, hm);
            }
            case HIDE_LORE -> {
                String context = data.context();
                if (context == null) { ApiInputHandler.remove(uuid); return; }
                HideItem hide = hm.getHide(context);
                if (hide == null) { ApiInputHandler.remove(uuid); return; }

                if (message.equalsIgnoreCase("finish")) {
                    hm.saveAll();
                    player.sendMessage(ColorUtil.colorize("#a8ff78✔ Описание сохранено!"));
                    ApiInputHandler.remove(uuid);
                    HideEditMenu.open(player, hide, hm);
                    return;
                }
                if (message.equalsIgnoreCase("clear")) {
                    hide.setLore(new ArrayList<>());
                    player.sendMessage(ColorUtil.colorize("#a8ff78✔ Описание очищено."));
                    return;
                }
                if (message.equalsIgnoreCase("del")) {
                    List<String> lines = new ArrayList<>(hide.getLore() != null ? hide.getLore() : List.of());
                    if (!lines.isEmpty()) lines.remove(lines.size() - 1);
                    hide.setLore(lines);
                    player.sendMessage(ColorUtil.colorize("#a8ff78✔ Последняя строка удалена."));
                    return;
                }
                List<String> lines = new ArrayList<>(hide.getLore() != null ? hide.getLore() : List.of());
                lines.add(ColorUtil.colorize(message));
                hide.setLore(lines);
                player.sendMessage(ColorUtil.colorize("#757575Добавлено. Ещё строки или #fffffffinish #757575для сохранения."));
            }
            case HIDE_MATERIAL -> {
                String context = data.context();
                if (context == null) { ApiInputHandler.remove(uuid); return; }
                HideItem hide = hm.getHide(context);
                if (hide == null) { ApiInputHandler.remove(uuid); return; }
                Material material = Material.matchMaterial(message.toUpperCase());
                if (material == null) {
                    player.sendMessage(ColorUtil.colorize("#FF4E5E⚠ Материал не найден."));
                    return;
                }
                hide.setMaterial(material);
                hm.saveAll();
                player.sendMessage(ColorUtil.colorize("#a8ff78✔ Материал: #ffffff" + material.name()));
                ApiInputHandler.remove(uuid);
                HideEditMenu.open(player, hide, hm);
            }
        }
    }
}
