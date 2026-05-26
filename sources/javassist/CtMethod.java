package javassist;

import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.MethodInfo;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/CtMethod.class */
public final class CtMethod extends CtBehavior {
    protected String cachedStringRep;

    CtMethod(MethodInfo minfo, CtClass declaring) {
        super(declaring, minfo);
        this.cachedStringRep = null;
    }

    public CtMethod(CtClass returnType, String mname, CtClass[] parameters, CtClass declaring) {
        this(null, declaring);
        ConstPool cp = declaring.getClassFile2().getConstPool();
        String desc = Descriptor.ofMethod(returnType, parameters);
        this.methodInfo = new MethodInfo(cp, mname, desc);
        setModifiers(1025);
    }

    public CtMethod(CtMethod src, CtClass declaring, ClassMap map) throws CannotCompileException {
        this(null, declaring);
        copy(src, false, map);
    }

    public static CtMethod make(String src, CtClass declaring) throws CannotCompileException {
        return CtNewMethod.make(src, declaring);
    }

    public static CtMethod make(MethodInfo minfo, CtClass declaring) throws CannotCompileException {
        if (declaring.getClassFile2().getConstPool() != minfo.getConstPool()) {
            throw new CannotCompileException("bad declaring class");
        }
        return new CtMethod(minfo, declaring);
    }

    public int hashCode() {
        return getStringRep().hashCode();
    }

    @Override // javassist.CtMember
    void nameReplaced() {
        this.cachedStringRep = null;
    }

    final String getStringRep() {
        if (this.cachedStringRep == null) {
            this.cachedStringRep = this.methodInfo.getName() + Descriptor.getParamDescriptor(this.methodInfo.getDescriptor());
        }
        return this.cachedStringRep;
    }

    public boolean equals(Object obj) {
        return obj != null && (obj instanceof CtMethod) && ((CtMethod) obj).getStringRep().equals(getStringRep());
    }

    @Override // javassist.CtBehavior
    public String getLongName() {
        return getDeclaringClass().getName() + "." + getName() + Descriptor.toString(getSignature());
    }

    @Override // javassist.CtMember
    public String getName() {
        return this.methodInfo.getName();
    }

    public void setName(String newname) {
        this.declaringClass.checkModify();
        this.methodInfo.setName(newname);
    }

    public CtClass getReturnType() throws NotFoundException {
        return getReturnType0();
    }

    @Override // javassist.CtBehavior
    public boolean isEmpty() {
        CodeAttribute ca = getMethodInfo2().getCodeAttribute();
        if (ca == null) {
            return (getModifiers() & 1024) != 0;
        }
        CodeIterator it = ca.iterator();
        try {
            if (it.hasNext() && it.byteAt(it.next()) == 177) {
                if (!it.hasNext()) {
                    return true;
                }
            }
            return false;
        } catch (BadBytecode e) {
            return false;
        }
    }

    public void setBody(CtMethod src, ClassMap map) throws CannotCompileException {
        setBody0(src.declaringClass, src.methodInfo, this.declaringClass, this.methodInfo, map);
    }

    public void setWrappedBody(CtMethod mbody, ConstParameter constParam) throws CannotCompileException {
        this.declaringClass.checkModify();
        CtClass clazz = getDeclaringClass();
        try {
            CtClass[] params = getParameterTypes();
            CtClass retType = getReturnType();
            Bytecode code = CtNewWrappedMethod.makeBody(clazz, clazz.getClassFile2(), mbody, params, retType, constParam);
            CodeAttribute cattr = code.toCodeAttribute();
            this.methodInfo.setCodeAttribute(cattr);
            this.methodInfo.setAccessFlags(this.methodInfo.getAccessFlags() & (-1025));
        } catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/CtMethod$ConstParameter.class */
    public static class ConstParameter {
        public static ConstParameter integer(int i) {
            return new IntConstParameter(i);
        }

        public static ConstParameter integer(long i) {
            return new LongConstParameter(i);
        }

        public static ConstParameter string(String s) {
            return new StringConstParameter(s);
        }

        ConstParameter() {
        }

        int compile(Bytecode code) throws CannotCompileException {
            return 0;
        }

        String descriptor() {
            return defaultDescriptor();
        }

        static String defaultDescriptor() {
            return "([Ljava/lang/Object;)Ljava/lang/Object;";
        }

        String constDescriptor() {
            return defaultConstDescriptor();
        }

        static String defaultConstDescriptor() {
            return "([Ljava/lang/Object;)V";
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/CtMethod$IntConstParameter.class */
    static class IntConstParameter extends ConstParameter {
        int param;

        IntConstParameter(int i) {
            this.param = i;
        }

        @Override // javassist.CtMethod.ConstParameter
        int compile(Bytecode code) throws CannotCompileException {
            code.addIconst(this.param);
            return 1;
        }

        @Override // javassist.CtMethod.ConstParameter
        String descriptor() {
            return "([Ljava/lang/Object;I)Ljava/lang/Object;";
        }

        @Override // javassist.CtMethod.ConstParameter
        String constDescriptor() {
            return "([Ljava/lang/Object;I)V";
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/CtMethod$LongConstParameter.class */
    static class LongConstParameter extends ConstParameter {
        long param;

        LongConstParameter(long l) {
            this.param = l;
        }

        @Override // javassist.CtMethod.ConstParameter
        int compile(Bytecode code) throws CannotCompileException {
            code.addLconst(this.param);
            return 2;
        }

        @Override // javassist.CtMethod.ConstParameter
        String descriptor() {
            return "([Ljava/lang/Object;J)Ljava/lang/Object;";
        }

        @Override // javassist.CtMethod.ConstParameter
        String constDescriptor() {
            return "([Ljava/lang/Object;J)V";
        }
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/CtMethod$StringConstParameter.class */
    static class StringConstParameter extends ConstParameter {
        String param;

        StringConstParameter(String s) {
            this.param = s;
        }

        @Override // javassist.CtMethod.ConstParameter
        int compile(Bytecode code) throws CannotCompileException {
            code.addLdc(this.param);
            return 1;
        }

        @Override // javassist.CtMethod.ConstParameter
        String descriptor() {
            return "([Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;";
        }

        @Override // javassist.CtMethod.ConstParameter
        String constDescriptor() {
            return "([Ljava/lang/Object;Ljava/lang/String;)V";
        }
    }
}
