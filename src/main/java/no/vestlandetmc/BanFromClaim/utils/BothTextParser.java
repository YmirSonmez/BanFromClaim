package no.vestlandetmc.BanFromClaim.utils;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class BothTextParser {

    private static final MiniMessage miniMessageInstance;
    private static final Char2ObjectMap<String> LEGACY_TO_TAG_MAP;

    static {
        MiniMessage.Builder builder = MiniMessage.builder()
                .preProcessor(BothTextParser::legacyToMiniMessage);

        try {
            Method emitVirtualsMethod = builder.getClass().getMethod("emitVirtuals", boolean.class);
            emitVirtualsMethod.invoke(builder, false);
        } catch (Exception e) {
        }

        miniMessageInstance = builder.build();
        final Char2ObjectMap<String> map = new Char2ObjectOpenHashMap<>(22, 0.9f);
        map.put('0', "<black>");
        map.put('1', "<dark_blue>");
        map.put('2', "<dark_green>");
        map.put('3', "<dark_aqua>");
        map.put('4', "<dark_red>");
        map.put('5', "<dark_purple>");
        map.put('6', "<gold>");
        map.put('7', "<gray>");
        map.put('8', "<dark_gray>");
        map.put('9', "<blue>");
        map.put('a', "<green>");
        map.put('b', "<aqua>");
        map.put('c', "<red>");
        map.put('d', "<light_purple>");
        map.put('e', "<yellow>");
        map.put('f', "<white>");
        map.put('k', "<obfuscated>");
        map.put('l', "<bold>");
        map.put('m', "<strikethrough>");
        map.put('n', "<underlined>");
        map.put('o', "<italic>");
        map.put('r', "<reset>");

        LEGACY_TO_TAG_MAP = map;
    }

    public static Component parse(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return miniMessageInstance.deserialize("<!i>" + text);
    }


    public static List<Component> parseList(List<String> textList) {
        if (textList == null || textList.isEmpty()) {
            return Collections.emptyList();
        }
        return textList.stream()
                .map(BothTextParser::parse)
                .collect(Collectors.toList());
    }

    public static String legacyToMiniMessage(String text) {
        if (text == null || (text.indexOf('&') == -1 && text.indexOf('§') == -1)) {
            return text;
        }

        char[] chars = text.toCharArray();
        StringBuilder builder = new StringBuilder(text.length() + 32);

        for (int i = 0; i < chars.length; i++) {
            char currentChar = chars[i];

            if ((currentChar == '&' || currentChar == '§') && i + 1 < chars.length) {
                char code = chars[i + 1];

                if ((code == 'x' || code == 'X') && i + 13 < chars.length) {
                    StringBuilder hexBuilder = new StringBuilder(6);
                    boolean isProperHex = true;
                    for (int j = 0; j < 6; j++) {
                        char indicator = chars[i + 2 + (j * 2)];
                        if (indicator != currentChar) {
                            isProperHex = false;
                            break;
                        }
                        hexBuilder.append(chars[i + 3 + (j * 2)]);
                    }

                    if (isProperHex) {
                        builder.append("<#").append(hexBuilder).append(">");
                        i += 13;
                        continue;
                    }
                }

                if (code == '#' && i + 7 < chars.length) {
                    builder.append("<#").append(new String(chars, i + 2, 6)).append(">");
                    i += 7;
                    continue;
                }

                String tag = LEGACY_TO_TAG_MAP.get(Character.toLowerCase(code));
                if (tag != null) {
                    builder.append(tag);
                    i++;
                } else {
                    builder.append(currentChar);
                }
            } else {
                builder.append(currentChar);
            }
        }
        return builder.toString();
    }
}
