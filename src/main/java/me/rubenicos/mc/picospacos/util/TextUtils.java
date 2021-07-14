package me.rubenicos.mc.picospacos.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class TextUtils {

    private static final Map<String, Pattern> regexCache = new HashMap<>();

    public static boolean regexMatch(String regex, String text) {
        return regexCache.getOrDefault(regex, newPattern(regex)).matcher(text).matches();
    }

    private static Pattern newPattern(String regex) {
        Pattern pattern = Pattern.compile(regex);
        regexCache.put(regex, pattern);
        return pattern;
    }

    public static int asInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e){
            return def;
        }
    }

    public static double asDouble(String s, double def) {
        try {
            return Double.parseDouble(s);
        } catch (NullPointerException | NumberFormatException e){
            return def;
        }
    }
}
