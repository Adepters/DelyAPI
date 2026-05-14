package me.dely.delyApi.input;

import me.dely.delyApi.hides.HideManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ApiInputHandler {

    public enum InputType {
        CREATE_HIDE,
        HIDE_NAME,
        HIDE_LORE,
        HIDE_MATERIAL
    }

    public record InputData(InputType type, String context, HideManager hideManager) {}

    private static final Map<UUID, InputData> waiting = new HashMap<>();

    public static void add(UUID uuid, InputType type, HideManager hideManager) {
        waiting.put(uuid, new InputData(type, null, hideManager));
    }

    public static void add(UUID uuid, InputType type, String context, HideManager hideManager) {
        waiting.put(uuid, new InputData(type, context, hideManager));
    }

    public static boolean isAwaiting(UUID uuid) {
        return waiting.containsKey(uuid);
    }

    public static InputData getData(UUID uuid) {
        return waiting.get(uuid);
    }

    public static void remove(UUID uuid) {
        waiting.remove(uuid);
    }
}
