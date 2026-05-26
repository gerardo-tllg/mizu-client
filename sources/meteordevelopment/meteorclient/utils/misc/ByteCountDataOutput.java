package meteordevelopment.meteorclient.utils.misc;

import java.io.DataOutput;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/misc/ByteCountDataOutput.class */
public class ByteCountDataOutput implements DataOutput {
    public static final ByteCountDataOutput INSTANCE = new ByteCountDataOutput();
    private int count;

    public int getCount() {
        return this.count;
    }

    public void reset() {
        this.count = 0;
    }

    @Override // java.io.DataOutput
    public void write(int b) throws IOException {
        this.count++;
    }

    @Override // java.io.DataOutput
    public void write(byte[] b) throws IOException {
        this.count += b.length;
    }

    @Override // java.io.DataOutput
    public void write(byte[] b, int off, int len) throws IOException {
        this.count += len;
    }

    @Override // java.io.DataOutput
    public void writeBoolean(boolean v) {
        this.count++;
    }

    @Override // java.io.DataOutput
    public void writeByte(int v) {
        this.count++;
    }

    @Override // java.io.DataOutput
    public void writeShort(int v) {
        this.count += 2;
    }

    @Override // java.io.DataOutput
    public void writeChar(int v) {
        this.count += 2;
    }

    @Override // java.io.DataOutput
    public void writeInt(int v) {
        this.count += 4;
    }

    @Override // java.io.DataOutput
    public void writeLong(long v) {
        this.count += 8;
    }

    @Override // java.io.DataOutput
    public void writeFloat(float v) {
        this.count += 4;
    }

    @Override // java.io.DataOutput
    public void writeDouble(double v) {
        this.count += 8;
    }

    @Override // java.io.DataOutput
    public void writeBytes(String s) {
        this.count += s.length();
    }

    @Override // java.io.DataOutput
    public void writeChars(String s) {
        this.count += s.length() * 2;
    }

    @Override // java.io.DataOutput
    public void writeUTF(@NotNull String s) {
        this.count = (int) (((long) this.count) + 2 + getUTFLength(s));
    }

    long getUTFLength(String s) {
        long j;
        long j2;
        long utflen = 0;
        for (int cpos = 0; cpos < s.length(); cpos++) {
            char c = s.charAt(cpos);
            if (c >= 1 && c <= 127) {
                j = utflen;
                j2 = 1;
            } else if (c > 2047) {
                j = utflen;
                j2 = 3;
            } else {
                j = utflen;
                j2 = 2;
            }
            utflen = j + j2;
        }
        return utflen;
    }
}
