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
}
