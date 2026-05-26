package javassist.bytecode;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/bytecode/ConstantAttribute.class */
public class ConstantAttribute extends AttributeInfo {
    public static final String tag = "ConstantValue";

    ConstantAttribute(ConstPool cp, int n, DataInputStream in) throws IOException {
        super(cp, n, in);
    }

    public ConstantAttribute(ConstPool cp, int index) {
        super(cp, tag);
        byte[] bvalue = {(byte) (index >>> 8), (byte) index};
        set(bvalue);
    }

    public int getConstantValue() {
        return ByteArray.readU16bit(get(), 0);
    }

    @Override // javassist.bytecode.AttributeInfo
    public AttributeInfo copy(ConstPool newCp, Map<String, String> classnames) {
        int index = getConstPool().copy(getConstantValue(), newCp, classnames);
        return new ConstantAttribute(newCp, index);
    }
}
