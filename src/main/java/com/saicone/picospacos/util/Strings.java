package com.saicone.picospacos.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Strings {

    @NotNull
    public static String before(@NotNull String s, char before) {
        final int index = s.indexOf(before);
        return index < 0 ? s : s.substring(0, index);
    }

    public static int escapeIndexOf(@NotNull String s, char c) {
        return escapeIndexOf(s, c, 0);
    }

    public static int escapeIndexOf(@NotNull String s, char c, int fromIndex) {
        boolean escape = false;
        for (int i = fromIndex; i < s.length(); i++) {
            final char c1 = s.charAt(i);
            if (c1 == c) {
                if (escape) continue;
                return i;
            } else if (c1 == '\\') {
                escape = true;
            }
        }
        return -1;
    }

    public static int quote(int c) {
        return (c == '\'' || c == '"' || c == '`') ? c : -1;
    }

    @NotNull
    public static String[] splitQuoted(@NotNull String s, char separator) {
        if (s.isEmpty()) {
            return new String[] { s };
        }
        final List<String> result = new ArrayList<>();
        int quote = quote(s.charAt(0));
        int start = quote + 1;
        boolean escape = false;
        for (int i = start; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (escape) {
                if (c == quote) {
                    continue;
                }
            } else if (c == quote) {
                if (i + 1 >= s.length() || s.charAt(i + 1) != separator) {
                    quote = -1;
                    start--;
                    i = start - 1;
                    continue;
                }
            } else if (c == separator) {
                if (quote != -1) {
                    result.add(s.substring(start, i - 1));
                } else {
                    result.add(s.substring(start, i));
                }
            } else if (c == '\\') {
                escape = true;
            }
        }
        return result.toArray(new String[0]);
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
