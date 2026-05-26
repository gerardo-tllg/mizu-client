package javassist.bytecode.annotation;

import java.io.IOException;
import java.lang.reflect.Method;
import javassist.ClassPool;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/bytecode/annotation/EnumMemberValue.class */
public class EnumMemberValue extends MemberValue {
    int typeIndex;
    int valueIndex;

    public EnumMemberValue(int type, int value, ConstPool cp) {
        super('e', cp);
        this.typeIndex = type;
        this.valueIndex = value;
    }

    public EnumMemberValue(ConstPool cp) {
        super('e', cp);
        this.valueIndex = 0;
        this.typeIndex = 0;
    }

    @Override // javassist.bytecode.annotation.MemberValue
    Object getValue(ClassLoader cl, ClassPool cp, Method m) throws ClassNotFoundException {
        try {
            return getType(cl).getField(getValue()).get(null);
        } catch (IllegalAccessException e) {
            throw new ClassNotFoundException(getType() + "." + getValue());
        } catch (NoSuchFieldException e2) {
            throw new ClassNotFoundException(getType() + "." + getValue());
        }
    }

    @Override // javassist.bytecode.annotation.MemberValue
    Class<?> getType(ClassLoader cl) throws ClassNotFoundException {
        return loadClass(cl, getType());
    }

    public String getType() {
        return Descriptor.toClassName(this.cp.getUtf8Info(this.typeIndex));
    }

    public void setType(String typename) {
        this.typeIndex = this.cp.addUtf8Info(Descriptor.of(typename));
    }

    public String getValue() {
        return this.cp.getUtf8Info(this.valueIndex);
    }

    public void setValue(String name) {
        this.valueIndex = this.cp.addUtf8Info(name);
    }

    public String toString() {
        return getType() + "." + getValue();
    }

    @Override // javassist.bytecode.annotation.MemberValue
    public void write(AnnotationsWriter writer) throws IOException {
        writer.enumConstValue(this.cp.getUtf8Info(this.typeIndex), getValue());
    }

    @Override // javassist.bytecode.annotation.MemberValue
    public void accept(MemberValueVisitor visitor) {
        visitor.visitEnumMemberValue(this);
    }
}
