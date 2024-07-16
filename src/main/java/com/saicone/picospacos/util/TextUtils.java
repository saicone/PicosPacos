package com.saicone.picospacos.util;

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

    public static Object[] rangeShort(String string) {
        Object[] a = new Object[2];
        String[] s = string.split("\\|");
        try {
            a[0] = Short.parseShort(s[0]);
        } catch (NullPointerException | NumberFormatException e) {
            a[0] = s[0];
        }
        if (s.length > 1) {
            try {
                a[1] = Short.parseShort(s[1]);
            } catch (NullPointerException | NumberFormatException e) {
                a[1] = s[0];
            }
        } else {
            a[1] = (short) 0;
        }
        return a;
    }

    public static Object[] rangeInt(String string) {
        Object[] a = new Object[2];
        String[] s = string.split("\\|");
        try {
            a[0] = Integer.parseInt(s[0]);
        } catch (NullPointerException | NumberFormatException e) {
            a[0] = s[0];
        }
        if (s.length > 1) {
            try {
                a[1] = Integer.parseInt(s[1]);
            } catch (NullPointerException | NumberFormatException e) {
                a[1] = s[0];
            }
        } else {
            a[1] = 0;
        }
        return a;
    }

    public static Object[] rangeDouble(String string) {
        Object[] a = new Object[2];
        String[] s = string.split("\\|");
        try {
            a[0] = Double.parseDouble(s[0]);
        } catch (NullPointerException | NumberFormatException e) {
            a[0] = s[0];
        }
        if (s.length > 1) {
            try {
                a[1] = Double.parseDouble(s[1]);
            } catch (NullPointerException | NumberFormatException e) {
                a[1] = s[0];
            }
        } else {
            a[1] = 0D;
        }
        return a;
    }
}
