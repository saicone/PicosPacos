package com.saicone.picospacos.core.paco.rule;

import com.saicone.picospacos.PicosPacos;

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
        PicosPacos.log(2, "Unknown rule type '" + s + "', check rules.yml");
        return RuleType.DISABLED;
    }
}
