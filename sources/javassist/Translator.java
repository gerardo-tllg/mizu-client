package javassist;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/Translator.class */
public interface Translator {
    void start(ClassPool classPool) throws CannotCompileException, NotFoundException;

    void onLoad(ClassPool classPool, String str) throws CannotCompileException, NotFoundException;
}
