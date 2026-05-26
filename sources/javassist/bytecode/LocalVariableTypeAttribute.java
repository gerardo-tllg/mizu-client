package javassist.bytecode;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/bytecode/LocalVariableTypeAttribute.class */
public class LocalVariableTypeAttribute extends LocalVariableAttribute {
    public static final String tag = "LocalVariableTypeTable";

    public LocalVariableTypeAttribute(ConstPool cp) {
        super(cp, "LocalVariableTypeTable", new byte[2]);
        ByteArray.write16bit(0, this.info, 0);
    }

    LocalVariableTypeAttribute(ConstPool cp, int n, DataInputStream in) throws IOException {
        super(cp, n, in);
    }

    private LocalVariableTypeAttribute(ConstPool cp, byte[] dest) {
        super(cp, "LocalVariableTypeTable", dest);
    }

    @Override // javassist.bytecode.LocalVariableAttribute
    String renameEntry(String desc, String oldname, String newname) {
        return SignatureAttribute.renameClass(desc, oldname, newname);
    }

    @Override // javassist.bytecode.LocalVariableAttribute
    String renameEntry(String desc, Map<String, String> classnames) {
        return SignatureAttribute.renameClass(desc, classnames);
    }

    @Override // javassist.bytecode.LocalVariableAttribute
    LocalVariableAttribute makeThisAttr(ConstPool cp, byte[] dest) {
        return new LocalVariableTypeAttribute(cp, dest);
    }
}
