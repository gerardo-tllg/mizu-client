package meteordevelopment.meteorclient.util.animation;

public class TitleAnimation {
    private long startTime;
    private long duration;
    private float startValue;
    private float endValue;
    private Easing easing;
    private float currentValue;
    private boolean paused = false;
    private long pauseTime = 0;
    private boolean completed = false;

    public TitleAnimation(long duration, float startValue, float endValue, Easing easing) {
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
        this.startValue = startValue;
        this.endValue = endValue;
        this.easing = easing;
        this.currentValue = startValue;
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
        this.paused = false;
        this.pauseTime = 0;
        this.completed = false;
    }

    public void reset(float startValue, float endValue) {
        this.startTime = System.currentTimeMillis();
        this.startValue = startValue;
        this.endValue = endValue;
        this.currentValue = startValue;
        this.paused = false;
        this.pauseTime = 0;
        this.completed = false;
    }

    public void update(float delta) {
        if (!paused) {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.max(0.0f, Math.min(1.0f, (float) elapsed / duration));
            this.currentValue = startValue + (endValue - startValue) * easing.ease(progress);
            if (elapsed >= duration && !completed) {
                completed = true;
            }
        }
    }

    public float getValue() {
        if (!paused) {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.max(0.0f, Math.min(1.0f, (float) elapsed / duration));
            return startValue + (endValue - startValue) * easing.ease(progress);
        }
        return currentValue;
    }

    public boolean isDone() {
        return !paused && (System.currentTimeMillis() - startTime >= duration);
    }

    public float getProgress() {
        if (paused) {
            return (currentValue - startValue) / (endValue - startValue);
        }
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.max(0.0f, Math.min(1.0f, (float) elapsed / duration));
    }

    public enum Easing {
        LINEAR(p -> p),
        EASE_IN_QUAD(p -> p * p),
        EASE_OUT_QUAD(p -> p * (2 - p)),
        EASE_OUT_CUBIC(p -> 1 - (float) Math.pow(1 - p, 3)),
        EASE_IN_CUBIC(p -> (float) Math.pow(p, 3)),
        EASE_OUT_EXPO(p -> p >= 1 ? 1 : 1 - (float) Math.pow(2, -10 * p)),
        EASE_IN_EXPO(p -> p <= 0 ? 0 : (float) Math.pow(2, 10 * p - 10)),
        ELASTIC(p -> {
            float c4 = (2 * (float) Math.PI) / 3;
            return p <= 0 ? 0 : p >= 1 ? 1 :
                (float) (-Math.pow(2, 10 * p - 10) * Math.sin((p * 10 - 10.75) * c4));
        }),
        BOUNCE(p -> {
            float n1 = 7.5625f;
            float d1 = 2.75f;
            if (p < 1 / d1) return n1 * p * p;
            else if (p < 2 / d1) { p -= 1.5f / d1; return n1 * p * p + 0.75f; }
            else if (p < 2.5 / d1) { p -= 2.25f / d1; return n1 * p * p + 0.9375f; }
            else { p -= 2.625f / d1; return n1 * p * p + 0.984375f; }
        });

        private final EasingFunction function;

        Easing(EasingFunction function) { this.function = function; }

        public float ease(float progress) { return function.ease(progress); }

        @FunctionalInterface
        private interface EasingFunction { float ease(float progress); }
    }

    public static int rgba(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}
