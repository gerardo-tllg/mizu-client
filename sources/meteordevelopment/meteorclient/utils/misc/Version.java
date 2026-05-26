package meteordevelopment.meteorclient.utils.misc;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/Version.class */
public class Version {
    private final String string;
    private final int[] numbers = new int[3];

    public Version(String string) {
        this.string = string;
        String[] split = string.split("\\.");
        if (split.length != 3) {
            throw new IllegalArgumentException("Version string needs to have 3 numbers.");
        }
        for (int i = 0; i < 3; i++) {
            try {
                this.numbers[i] = Integer.parseInt(split[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Failed to parse version string.");
            }
        }
    }

    public boolean isZero() {
        return this.numbers[0] == 0 && this.numbers[1] == 0 && this.numbers[2] == 0;
    }

    public boolean isHigherThan(Version version) {
        for (int i = 0; i < 3; i++) {
            if (this.numbers[i] > version.numbers[i]) {
                return true;
            }
            if (this.numbers[i] < version.numbers[i]) {
                return false;
            }
        }
        return false;
    }

    public String toString() {
        return this.string;
    }
}
