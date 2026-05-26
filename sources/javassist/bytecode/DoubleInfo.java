package javassist.bytecode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/* JADX INFO: compiled from: ConstPool.java */
/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/bytecode/DoubleInfo.class */
class DoubleInfo extends ConstInfo {
    static final int tag = 6;
    double value;

    public DoubleInfo(double d, int index) {
        super(index);
        this.value = d;
    }

    public DoubleInfo(DataInputStream in, int index) throws IOException {
        super(index);
        this.value = in.readDouble();
    }

    public int hashCode() {
        long v = Double.doubleToLongBits(this.value);
        return (int) (v ^ (v >>> 32));
    }

    public boolean equals(Object obj) {
        return (obj instanceof DoubleInfo) && ((DoubleInfo) obj).value == this.value;
    }

    @Override // javassist.bytecode.ConstInfo
    public int getTag() {
        return 6;
    }

    @Override // javassist.bytecode.ConstInfo
    public int copy(ConstPool src, ConstPool dest, Map<String, String> map) {
        return dest.addDoubleInfo(this.value);
    }

    @Override // javassist.bytecode.ConstInfo
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(6);
        out.writeDouble(this.value);
    }

    @Override // javassist.bytecode.ConstInfo
    public void print(PrintWriter out) {
        out.print("Double ");
        out.println(this.value);
    }
}
