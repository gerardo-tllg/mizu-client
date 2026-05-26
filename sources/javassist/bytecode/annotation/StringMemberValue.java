package javassist.bytecode.annotation;

import java.io.IOException;
import java.lang.reflect.Method;
import javassist.ClassPool;
import javassist.bytecode.ConstPool;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/bytecode/annotation/StringMemberValue.class */
public class StringMemberValue extends MemberValue {
    int valueIndex;

    public StringMemberValue(int index, ConstPool cp) {
        super('s', cp);
        this.valueIndex = index;
    }

    public StringMemberValue(String str, ConstPool cp) {
        super('s', cp);
        setValue(str);
    }

    public StringMemberValue(ConstPool cp) {
        super('s', cp);
        setValue("");
    }

    @Override // javassist.bytecode.annotation.MemberValue
    Object getValue(ClassLoader cl, ClassPool cp, Method m) {
        return getValue();
    }

    @Override // javassist.bytecode.annotation.MemberValue
    Class<?> getType(ClassLoader cl) {
        return String.class;
    }

    public String getValue() {
        return this.cp.getUtf8Info(this.valueIndex);
    }

    public void setValue(String newValue) {
        this.valueIndex = this.cp.addUtf8Info(newValue);
    }

    public String toString() {
        return "\"" + getValue() + "\"";
    }

    @Override // javassist.bytecode.annotation.MemberValue
    public void write(AnnotationsWriter writer) throws IOException {
        writer.constValueIndex(getValue());
    }

    @Override // javassist.bytecode.annotation.MemberValue
    public void accept(MemberValueVisitor visitor) {
        visitor.visitStringMemberValue(this);
    }
}
