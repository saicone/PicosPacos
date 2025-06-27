package com.saicone.picospacos.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class Strings {

    @Nullable
    public static Long time(@Nullable String s, @NotNull TimeUnit toUnit) {
        if (s == null) {
            return null;
        }
        final String[] value = s.replace("  ", " ").replace("  ", " ").split(" ", 2);
        final long time = Long.parseLong(value[0]);
        final TimeUnit unit;
        if (value.length > 1) {
            String name = value[1].toUpperCase();
            if (!name.endsWith("S")) {
                name = name + "S";
            }
            unit = TimeUnit.valueOf(name);
        } else {
            unit = TimeUnit.SECONDS;
        }
        return toUnit.convert(time, unit);
    }

    @Nullable
    @Contract("!null, _ -> !null")
    public static Object map(@Nullable Object object, @NotNull UnaryOperator<String> mapper) {
        if (object instanceof Map) {
            final Map<Object, Object> map = new HashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                map.put(map(entry.getKey(), mapper), map(entry.getValue(), mapper));
            }
            return map;
        } else if (object instanceof Iterable) {
            final List<Object> list = new ArrayList<>();
            for (Object element : (Iterable<?>) object) {
                list.add(map(element, mapper));
            }
            return list;
        } else if (object instanceof String) {
            return mapper.apply((String) object);
        }
        return object;
    }

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
            if (escape) {
                escape = false;
                continue;
            }
            if (c1 == '\\') {
                escape = true;
            } else if (c1 == c) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isQuote(int c) {
        return c == '\'' || c == '"' || c == '`';
    }

    @NotNull
    public static String[] splitQuoted(@NotNull String s, char separator) {
        List<String> result = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;
        boolean escape = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (escape) {
                token.append(c);
                escape = false;
                continue;
            }

            if (c == '\\') {
                escape = true;
                continue;
            }

            if (inQuotes) {
                if (c == quoteChar) {
                    inQuotes = false;
                } else {
                    token.append(c);
                }
            } else {
                if (isQuote(c)) {
                    inQuotes = true;
                    quoteChar = c;
                } else if (c == separator) {
                    result.add(token.toString());
                    token.setLength(0);
                } else {
                    token.append(c);
                }
            }
        }

        result.add(token.toString());

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
