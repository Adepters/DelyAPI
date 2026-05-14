package me.dely.delyApi.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");

    public static String colorize(String message) {
        if (message == null) return null;

        Matcher matcher = HEX_PATTERN.matcher(message);
        while (matcher.find()) {
            String color = matcher.group();
            ChatColor hexColor = ChatColor.of(color);
            message = message.replace(color, hexColor.toString());
        }

        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }

    public static List<String> colorizeList(List<String> messages) {
        List<String> result = new ArrayList<>();
        for (String message : messages) result.add(colorize(message));
        return result;
    }
}
