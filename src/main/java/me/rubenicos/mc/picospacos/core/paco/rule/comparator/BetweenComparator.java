package me.rubenicos.mc.picospacos.core.paco.rule.comparator;

import me.rubenicos.mc.picospacos.core.paco.rule.ComparatorType;

public final class BetweenComparator implements ComparatorType {
    @Override
    public boolean compareDouble(double variable, double... base) {
        if (base.length > 1) {
            return base[0] < Math.max(base[0], base[1]) && variable > base[0];
        } else {
            return base[0] == variable;
        }
    }

    @Override
    public boolean compareInt(int variable, int... base) {
        if (base.length > 1) {
            return base[0] < Math.max(base[0], base[1]) && variable > base[0];
        } else {
            return base[0] == variable;
        }
    }

    @Override
    public boolean compareShort(short variable, short... base) {
        if (base.length > 1) {
            return base[0] < Math.max(base[0], base[1]) && variable > base[0];
        } else {
            return base[0] == variable;
        }
    }
}
