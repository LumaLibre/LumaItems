package dev.jsinco.luma.lumaitems.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import java.util.List;

public class MiniMessageUtil {

    public static final Component PREFIX = mm("<b><#b986f9>Info</b> <dark_gray>»<white> ");

    public static void msg(CommandSender sender, String m) {
        sender.sendMessage(PREFIX.append(mm(m)));
    }

    public static void msg(CommandSender sender, Component m) {
        sender.sendMessage(PREFIX.append(m));
    }

    public static Component mm(String m) {
        return MiniMessage.miniMessage().deserialize("<!i>" + m);
    }

    public static List<Component> mml(String m) {
        return List.of(mm(m));
    }

    public static List<Component> mml(List<String> m) {
        return m.stream().map(MiniMessageUtil::mm).toList();
    }

    public static List<Component> mml(String... m) {
        return List.of(m).stream().map(MiniMessageUtil::mm).toList();
    }


    /*public static String convertMiniMessageToLegacy(String miniMessage) {
        StringBuilder stringBuilder = new StringBuilder();

        String[] texts = miniMessage.split("<");
        for (String text : texts) {
            if (text.isEmpty()) continue;
            if (text.charAt(0) == '#') {
                stringBuilder.append("&").append(text);
            } else if (text.charAt(0) == '/') {
                stringBuilder.append("&").append(text.charAt(1));
            }

            else {
                stringBuilder.append("&").append(text.charAt(0));
            }
            stringBuilder.append(text.substring(1));
        }
    }*/


    public static String convertLegacyToMiniMesssageString(String legacy) {
        String[] texts = legacy.split(String.format(Util.WITH_DELIMITER, "&"));

        StringBuilder finalText = new StringBuilder();

        for (int i = 0; i < texts.length; i++) {
            if (texts[i].equalsIgnoreCase("&")) {
                //get the next string
                i++;
                if (texts[i].charAt(0) == '#') {
                    finalText.append("<").append(texts[i].substring(0, 7)).append(texts[i].substring(7) + ">");
                } else {
                    finalText.append(getMiniMessageNamedColor('&' + texts[i]));
                }
            } else {
                finalText.append(texts[i]);
            }
        }
        return finalText.toString();
    }


    public static String getMiniMessageNamedColor(String namedColor) {
        return switch (namedColor) {
            case "&0" -> "<black>";
            case "&1" -> "<dark_blue>";
            case "&2" -> "<dark_green>";
            case "&3" -> "<dark_aqua>";
            case "&4" -> "<dark_red>";
            case "&5" -> "<dark_purple>";
            case "&6" -> "<gold>";
            case "&7" -> "<gray>";
            case "&8" -> "<dark_gray>";
            case "&9" -> "<blue>";
            case "&a" -> "<green>";
            case "&b" -> "<aqua>";
            case "&c" -> "<red>";
            case "&d" -> "<light_purple>";
            case "&e" -> "<yellow>";
            case "&f" -> "<white>";
            case "&k" -> "<obf>";
            case "&l" -> "<b>";
            case "&m" -> "<st>";
            case "&n" -> "<u>";
            case "&o" -> "<i>";
            case "&r" -> "<reset>";
            default -> throw new IllegalStateException("Unexpected value: " + namedColor);
        };
    }

    /*public static String getLegacyNamedColor(String namedColor) {

    }*/
}
