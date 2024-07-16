package com.saicone.picospacos.util;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Strings {

    private static final Map<String, Pattern> regexCache = new HashMap<>();

    public static boolean regexMatch(@NotNull @Language("RegExp") String regex, @NotNull String text) {
        return regexCache.getOrDefault(regex, newPattern(regex)).matcher(text).matches();
    }

    @NotNull
    private static Pattern newPattern(@NotNull @Language("RegExp") String regex) {
        Pattern pattern = Pattern.compile(regex);
        regexCache.put(regex, pattern);
        return pattern;
    }

    @NotNull
    public static Object[] rangeShort(@NotNull String string) {
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

    @NotNull
    public static Object[] rangeInt(@NotNull String string) {
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

    @NotNull
    public static Object[] rangeDouble(@NotNull String string) {
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

    @NotNull
    public static String replaceArgs(@NotNull String s, @Nullable Object... args) {
        if (args.length < 1 || s.isBlank()) {
            return s.replace("{#}", "0").replace("{*}", "[]").replace("{-}", "");
        }
        final char[] chars = s.toCharArray();
        final StringBuilder builder = new StringBuilder(s.length());
        String all = null;
        for (int i = 0; i < chars.length; i++) {
            final int mark = i;
            if (chars[i] == '{') {
                int num = 0;
                while (i + 1 < chars.length) {
                    if (Character.isDigit(chars[i + 1])) {
                        i++;
                        num *= 10;
                        num += chars[i] - '0';
                        continue;
                    }
                    if (i == mark) {
                        final char c = chars[i + 1];
                        if (c == '#') {
                            i++;
                            num = -1;
                        } else if (c == '*') {
                            i++;
                            num = -2;
                        } else if (c == '-') {
                            i++;
                            num = -3;
                        }
                    }
                    break;
                }
                if (i != mark && i + 1 < chars.length && chars[i + 1] == '}') {
                    i++;
                    if (num == -1) {
                        builder.append(args.length);
                    } else if (num == -2) {
                        builder.append(Arrays.toString(args));
                    } else if (num == -3) {
                        if (all == null) {
                            all = Arrays.stream(args).map(String::valueOf).collect(Collectors.joining(" "));
                        }
                        builder.append(all);
                    } else if (num < args.length) { // Avoid IndexOutOfBoundsException
                        builder.append(args[num]);
                    } else {
                        builder.append('{').append(num).append('}');
                    }
                } else {
                    i = mark;
                }
            }
            if (mark == i) {
                builder.append(chars[i]);
            }
        }
        return builder.toString();
    }
}
