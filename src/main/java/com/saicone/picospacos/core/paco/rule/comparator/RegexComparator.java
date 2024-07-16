package com.saicone.picospacos.core.paco.rule.comparator;

import com.saicone.picospacos.core.paco.rule.ComparatorType;
import com.saicone.picospacos.util.Strings;

import java.util.List;

public final class RegexComparator implements ComparatorType {
    @Override
    public boolean compareString(String base, String variable) {
        return Strings.regexMatch(base, variable);
    }

    @Override
    public boolean compareList(List<String> base, List<String> variable) {
        for (String s : variable) {
            for (String s1 : base) {
                if (Strings.regexMatch(s1, s)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean compareListAll(List<String> base, List<String> variable) {
        for (String s : base) {
            for (String s1 : variable) {
                if (!Strings.regexMatch(s, s1)) return false;
            }
        }
        return true;
    }
}
