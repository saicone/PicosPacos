package me.rubenicos.mc.picospacos.core.paco.rule;

import java.util.List;

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

    default boolean compareList(List<String> base, List<String> variable) {
        for (String s : variable) {
            if (base.contains(s)) return true;
        }
        return false;
    }

    default boolean compareListAll(List<String> base, List<String> variable) {
        return variable.containsAll(base);
    }

}
