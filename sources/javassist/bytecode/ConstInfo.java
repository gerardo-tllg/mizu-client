package javassist.bytecode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/* JADX INFO: compiled from: ConstPool.java */
/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/bytecode/ConstInfo.class */
abstract class ConstInfo {
    int index;

    public abstract int getTag();

    public abstract int copy(ConstPool constPool, ConstPool constPool2, Map<String, String> map);

    public abstract void write(DataOutputStream dataOutputStream) throws IOException;

    public abstract void print(PrintWriter printWriter);

    public ConstInfo(int i) {
        this.index = i;
    }

    public String getClassName(ConstPool cp) {
        return null;
    }

    public void renameClass(ConstPool cp, String oldName, String newName, Map<ConstInfo, ConstInfo> cache) {
    }

    public void renameClass(ConstPool cp, Map<String, String> classnames, Map<ConstInfo, ConstInfo> cache) {
    }

    public String toString() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(bout);
        print(out);
        return bout.toString();
    }
}
