package meteordevelopment.meteorclient.utils.player;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/player/Timer.class */
public class Timer {
    private long time = -1;

    public Timer() {
        reset();
    }

    public Timer reset() {
        this.time = System.nanoTime();
        return this;
    }

    public boolean tick(int tick) {
        return passedMs(((long) tick) * 50);
    }

    public boolean passedTicks(int tick) {
        return passedMs(((long) tick) * 50);
    }

    public boolean passedS(double s) {
        return passedMs(s * 1000.0d);
    }

    public boolean passedMs(long ms) {
        return passedNS(convertToNS(ms));
    }

    public boolean passedMs(double ms) {
        return passedMs((long) ms);
    }

    public boolean passed(long ms) {
        return passedMs(ms);
    }

    public boolean passed(double ms) {
        return passedMs((long) ms);
    }

    public void setMs(long ms) {
        this.time = System.nanoTime() - convertToNS(ms);
    }

    public boolean passedNS(long ns) {
        return System.nanoTime() - this.time >= ns;
    }

    public long getPassedTimeMs() {
        return getMs(System.nanoTime() - this.time);
    }

    public long getMs(long time) {
        return time / 1000000;
    }

    public long convertToNS(long time) {
        return time * 1000000;
    }
}
