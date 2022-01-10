package me.rubenicos.mc.picospacos.core.paco.rule;

import me.rubenicos.mc.picospacos.module.Locale;

public enum RuleType {

    DEATH, DROP, NODROP, DISABLED;

    public static RuleType of(String s) {
        for (RuleType value : values()) {
            if (value.name().equalsIgnoreCase(s)) {
                return value;
            }
        }
        Locale.sendToConsole("Paco.Error.Rule-Type", s);
        return RuleType.DISABLED;
    }
}
