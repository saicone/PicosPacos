package com.saicone.picospacos.api.item;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

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

    @NotNull
    public static Delay delay(long time, @NotNull TimeUnit unit) {
        return new Delay(time, unit);
    }

    public boolean isDone() {
        return false;
    }

    public boolean isBreak() {
        return false;
    }

    public static class Delay extends ActionResult {

        private final long time;
        private final TimeUnit unit;

        public Delay(long time, @NotNull TimeUnit unit) {
            this.time = time;
            this.unit = unit;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        public long time() {
            return time;
        }

        @NotNull
        public TimeUnit unit() {
            return unit;
        }

        public long ticks() {
            return (long) (unit().toMillis(time()) * 0.02);
        }
    }
}
