package com.saicone.picospacos.core.paco.rule;

import com.saicone.picospacos.module.Locale;

public enum RuleType {

    DEATH,
    DROP,
    NODROP,
    DELETE,
    DISABLED;

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
