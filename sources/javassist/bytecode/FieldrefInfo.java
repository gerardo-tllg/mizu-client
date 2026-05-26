package javassist.bytecode;

import java.io.DataInputStream;
import java.io.IOException;

/* JADX INFO: compiled from: ConstPool.java */
/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/bytecode/FieldrefInfo.class */
class FieldrefInfo extends MemberrefInfo {
    static final int tag = 9;

    public FieldrefInfo(int cindex, int ntindex, int thisIndex) {
        super(cindex, ntindex, thisIndex);
    }

    public FieldrefInfo(DataInputStream in, int thisIndex) throws IOException {
        super(in, thisIndex);
    }

    @Override // javassist.bytecode.ConstInfo
    public int getTag() {
        return 9;
    }

    @Override // javassist.bytecode.MemberrefInfo
    public String getTagName() {
        return "Field";
    }

    @Override // javassist.bytecode.MemberrefInfo
    protected int copy2(ConstPool dest, int cindex, int ntindex) {
        return dest.addFieldrefInfo(cindex, ntindex);
    }
}
