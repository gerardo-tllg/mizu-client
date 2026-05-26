package meteordevelopment.meteorclient.util.animation;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/util/animation/TitleAnimation.class */
public class TitleAnimation {
    private long duration;
    private float startValue;
    private float endValue;
    private Easing easing;
    private float currentValue;
    private boolean paused = false;
    private long pauseTime = 0;
    private boolean completed = false;
    private long startTime = System.currentTimeMillis();

    public TitleAnimation(long duration, float startValue, float endValue, Easing easing) {
        this.duration = duration;
        this.startValue = startValue;
        this.endValue = endValue;
        this.easing = easing;
        this.currentValue = startValue;
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
        this.paused = false;
        this.pauseTime = 0L;
        this.completed = false;
    }

    public void reset(float startValue, float endValue) {
        this.startTime = System.currentTimeMillis();
        this.startValue = startValue;
        this.endValue = endValue;
        this.currentValue = startValue;
        this.paused = false;
        this.pauseTime = 0L;
        this.completed = false;
    }

    public void update(float delta) {
        if (!this.paused) {
            long elapsed = System.currentTimeMillis() - this.startTime;
            float progress = Math.max(0.0f, Math.min(1.0f, elapsed / this.duration));
            this.currentValue = this.startValue + ((this.endValue - this.startValue) * this.easing.ease(progress));
            if (elapsed >= this.duration && !this.completed) {
                this.completed = true;
            }
        }
    }

    public float getValue() {
        if (!this.paused) {
            long elapsed = System.currentTimeMillis() - this.startTime;
            float progress = Math.max(0.0f, Math.min(1.0f, elapsed / this.duration));
            return this.startValue + ((this.endValue - this.startValue) * this.easing.ease(progress));
        }
        return this.currentValue;
    }

    public boolean isDone() {
        return !this.paused && System.currentTimeMillis() - this.startTime >= this.duration;
    }

    public float getProgress() {
        if (this.paused) {
            return (this.currentValue - this.startValue) / (this.endValue - this.startValue);
        }
        long elapsed = System.currentTimeMillis() - this.startTime;
        return Math.max(0.0f, Math.min(1.0f, elapsed / this.duration));
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/util/animation/TitleAnimation$Easing.class */
    public enum Easing {
        LINEAR(p -> {
            return p;
        }),
        EASE_IN_QUAD(p2 -> {
            return p2 * p2;
        }),
        EASE_OUT_QUAD(p3 -> {
            return p3 * (2.0f - p3);
        }),
        EASE_OUT_CUBIC(p4 -> {
            return 1.0f - ((float) Math.pow(1.0f - p4, 3.0d));
        }),
        EASE_IN_CUBIC(p5 -> {
            return (float) Math.pow(p5, 3.0d);
        }),
        EASE_OUT_EXPO(p6 -> {
            if (p6 >= 1.0f) {
                return 1.0f;
            }
            return 1.0f - ((float) Math.pow(2.0d, (-10.0f) * p6));
        }),
        EASE_IN_EXPO(p7 -> {
            if (p7 <= 0.0f) {
                return 0.0f;
            }
            return (float) Math.pow(2.0d, (10.0f * p7) - 10.0f);
        }),
        ELASTIC(p8 -> {
            if (p8 <= 0.0f) {
                return 0.0f;
            }
            if (p8 >= 1.0f) {
                return 1.0f;
            }
            return (float) ((-Math.pow(2.0d, (10.0f * p8) - 10.0f)) * Math.sin((((double) (p8 * 10.0f)) - 10.75d) * ((double) 2.0943952f)));
        }),
        BOUNCE(p9 -> {
            if (p9 < 1.0f / 2.75f) {
                return 7.5625f * p9 * p9;
            }
            if (p9 < 2.0f / 2.75f) {
                float p9 = p9 - (1.5f / 2.75f);
                return (7.5625f * p9 * p9) + 0.75f;
            }
            if (p9 < 2.5d / ((double) 2.75f)) {
                float p10 = p9 - (2.25f / 2.75f);
                return (7.5625f * p10 * p10) + 0.9375f;
            }
            float p11 = p9 - (2.625f / 2.75f);
            return (7.5625f * p11 * p11) + 0.984375f;
        });

        private final EasingFunction function;

        /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/util/animation/TitleAnimation$Easing$EasingFunction.class */
        @FunctionalInterface
        private interface EasingFunction {
            float ease(float f);
        }

        Easing(EasingFunction function) {
            this.function = function;
        }

        public float ease(float progress) {
            return this.function.ease(progress);
        }
    }

    public static int rgba(int r, int g, int b, int a) {
        return ((a & 255) << 24) | ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
    }
}
