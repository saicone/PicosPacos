package com.saicone.picospacos.api.item;

public class ActionResult {

    public static final ActionResult DONE = new ActionResult() {
        @Override
        public boolean isDone() {
            return true;
        }
    };

    public static final ActionResult BREAK = new ActionResult() {
        @Override
        public boolean isBreak() {
            return true;
        }
    };

    public boolean isDone() {
        return false;
    }

    public boolean isBreak() {
        return false;
    }
}
