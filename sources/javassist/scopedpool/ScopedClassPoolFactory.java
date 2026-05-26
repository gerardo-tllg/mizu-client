package javassist.scopedpool;

import javassist.ClassPool;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:javassist/scopedpool/ScopedClassPoolFactory.class */
public interface ScopedClassPoolFactory {
    ScopedClassPool create(ClassLoader classLoader, ClassPool classPool, ScopedClassPoolRepository scopedClassPoolRepository);

    ScopedClassPool create(ClassPool classPool, ScopedClassPoolRepository scopedClassPoolRepository);
}
