package com.saicone.picospacos.core.paco.rule.comparator;

import com.saicone.picospacos.core.paco.rule.ComparatorType;

import java.util.List;

public final class ContainsComparator implements ComparatorType {
    @Override
    public boolean compareString(String base, String variable) {
        return variable.contains(base);
    }

    @Override
    public boolean compareList(List<String> base, List<String> variable) {
        for (String s : variable) {
            for (String s1 : base) {
                if (s.contains(s1)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean compareListAll(List<String> base, List<String> variable) {
        for (String s : base) {
            for (String s1 : variable) {
                if (!s1.contains(s)) return false;
            }
        }
        return true;
    }
}
