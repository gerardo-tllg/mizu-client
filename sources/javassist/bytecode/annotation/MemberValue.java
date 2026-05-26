package javassist.bytecode.annotation;

import java.io.IOException;
import java.lang.reflect.Method;
import javassist.ClassPool;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/bytecode/annotation/MemberValue.class */
public abstract class MemberValue {
    ConstPool cp;
    char tag;

    abstract Object getValue(ClassLoader classLoader, ClassPool classPool, Method method) throws ClassNotFoundException;

    abstract Class<?> getType(ClassLoader classLoader) throws ClassNotFoundException;

    public abstract void accept(MemberValueVisitor memberValueVisitor);

    public abstract void write(AnnotationsWriter annotationsWriter) throws IOException;

    MemberValue(char tag, ConstPool cp) {
        this.cp = cp;
        this.tag = tag;
    }

    static Class<?> loadClass(ClassLoader cl, String classname) throws ClassNotFoundException, NoSuchClassError {
        try {
            return Class.forName(convertFromArray(classname), true, cl);
        } catch (LinkageError e) {
            throw new NoSuchClassError(classname, e);
        }
    }

    private static String convertFromArray(String classname) {
        int index = classname.indexOf("[]");
        if (index != -1) {
            String rawType = classname.substring(0, index);
            StringBuffer sb = new StringBuffer(Descriptor.of(rawType));
            while (index != -1) {
                sb.insert(0, "[");
                index = classname.indexOf("[]", index + 1);
            }
            return sb.toString().replace('/', '.');
        }
        return classname;
    }
}
