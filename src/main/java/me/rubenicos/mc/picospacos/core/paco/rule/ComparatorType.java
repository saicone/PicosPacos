package me.rubenicos.mc.picospacos.core.paco.rule;

import me.rubenicos.mc.picospacos.util.TextUtils;

import java.util.List;

public class ComparatorType {

    public static ComparatorType of(String name) {
        switch (name.toLowerCase()) {
            case "contains":
                return new Contains();
            case "regex":
                return new Regex();
            case "more":
                return new More();
            case "less":
                return new Less();
            case "between":
                return new Between();
            default:
                return new ComparatorType();
        }
    }

    public boolean compare(String base, String variable) {
        return base.equals(variable);
    }

    public boolean compareDouble(double variable, double... base) {
        return base[0] == variable;
    }

    public boolean compare(List<String> base, List<String> variable) {
        for (String s : variable) {
            if (base.contains(s)) return true;
        }
        return false;
    }

    public boolean compareAll(List<String> base, List<String> variable) {
        return variable.containsAll(base);
    }

    private static final class Contains extends ComparatorType {
        @Override
        public boolean compare(String base, String variable) {
            return variable.contains(base);
        }

        @Override
        public boolean compare(List<String> base, List<String> variable) {
            for (String s : variable) {
                for (String s1 : base) {
                    if (s.contains(s1)) return true;
                }
            }
            return false;
        }

        @Override
        public boolean compareAll(List<String> base, List<String> variable) {
            for (String s : base) {
                for (String s1 : variable) {
                    if (!s1.contains(s)) return false;
                }
            }
            return true;
        }
    }

    private static final class Regex extends ComparatorType {
        @Override
        public boolean compare(String base, String variable) {
            return TextUtils.regexMatch(base, variable);
        }

        @Override
        public boolean compare(List<String> base, List<String> variable) {
            for (String s : variable) {
                for (String s1 : base) {
                    if (TextUtils.regexMatch(s1, s)) return true;
                }
            }
            return false;
        }

        @Override
        public boolean compareAll(List<String> base, List<String> variable) {
            for (String s : base) {
                for (String s1 : variable) {
                    if (!TextUtils.regexMatch(s, s1)) return false;
                }
            }
            return true;
        }
    }

    private static final class More extends ComparatorType {
        @Override
        public boolean compareDouble(double variable, double... base) {
            return base[0] < variable;
        }
    }

    private static final class Less extends ComparatorType {
        @Override
        public boolean compareDouble(double variable, double... base) {
            return base[0] > variable;
        }
    }

    private static final class Between extends ComparatorType {
        @Override
        public boolean compareDouble(double variable, double... base) {
            if (base.length > 1) {
                return base[0] < Math.max(base[0], base[1]) && variable > base[0];
            } else {
                return base[0] == variable;
            }
        }
    }
}
