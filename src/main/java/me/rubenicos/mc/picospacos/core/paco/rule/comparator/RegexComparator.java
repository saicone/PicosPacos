package me.rubenicos.mc.picospacos.core.paco.rule.comparator;

import me.rubenicos.mc.picospacos.core.paco.rule.ComparatorType;
import me.rubenicos.mc.picospacos.util.TextUtils;

import java.util.List;

public final class RegexComparator implements ComparatorType {
    @Override
    public boolean compareString(String base, String variable) {
        return TextUtils.regexMatch(base, variable);
    }

    @Override
    public boolean compareList(List<String> base, List<String> variable) {
        for (String s : variable) {
            for (String s1 : base) {
                if (TextUtils.regexMatch(s1, s)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean compareListAll(List<String> base, List<String> variable) {
        for (String s : base) {
            for (String s1 : variable) {
                if (!TextUtils.regexMatch(s, s1)) return false;
            }
        }
        return true;
    }
}
