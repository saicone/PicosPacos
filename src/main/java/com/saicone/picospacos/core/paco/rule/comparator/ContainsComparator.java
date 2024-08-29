package com.saicone.picospacos.core.paco.rule.comparator;

import com.saicone.picospacos.core.paco.rule.ComparatorType;

import java.util.Collection;

public final class ContainsComparator implements ComparatorType {
    @Override
    public boolean compareString(String base, String variable) {
        return variable.contains(base);
    }

    @Override
    public boolean compareList(Collection<String> base, Collection<String> variable) {
        for (String s : variable) {
            for (String s1 : base) {
                if (s.contains(s1)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean compareListAll(Collection<String> base, Collection<String> variable) {
        for (String s : base) {
            for (String s1 : variable) {
                if (!s1.contains(s)) return false;
            }
        }
        return true;
    }
}
