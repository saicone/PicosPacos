package com.saicone.picospacos.core.paco.rule;

import java.util.Collection;

public interface ComparatorType {

    default boolean compareString(String base, String variable) {
        return base.equals(variable);
    }

    default boolean compareDouble(double variable, double... base) {
        return base[0] == variable;
    }

    default boolean compareInt(int variable, int... base) {
        return base[0] == variable;
    }

    default boolean compareShort(short variable, short... base) {
        return base[0] == variable;
    }

    default boolean compareList(Collection<String> base, Collection<String> variable) {
        for (String s : variable) {
            if (base.contains(s)) return true;
        }
        return false;
    }

    default boolean compareListAll(Collection<String> base, Collection<String> variable) {
        return variable.containsAll(base);
    }

}
