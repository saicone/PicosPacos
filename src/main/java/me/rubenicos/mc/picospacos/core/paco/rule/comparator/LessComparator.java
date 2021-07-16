package me.rubenicos.mc.picospacos.core.paco.rule.comparator;

import me.rubenicos.mc.picospacos.core.paco.rule.ComparatorType;

public final class LessComparator implements ComparatorType {
    @Override
    public boolean compareDouble(double variable, double... base) {
        return base[0] > variable;
    }

    @Override
    public boolean compareInt(int variable, int... base) {
        return base[0] > variable;
    }

    @Override
    public boolean compareShort(short variable, short... base) {
        return base[0] > variable;
    }
}
