package org.reflections.scanners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;
import javax.annotation.Nonnull;
import org.reflections.ReflectionsException;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.JavassistHelper;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/scanners/MemberUsageScanner.class */
public class MemberUsageScanner implements Scanner {
    private Predicate<String> resultFilter;
    private final ClassLoader[] classLoaders;
    private volatile ClassPool classPool;

    public MemberUsageScanner() {
        this(ClasspathHelper.classLoaders(new ClassLoader[0]));
    }

    public MemberUsageScanner(@Nonnull ClassLoader[] classLoaders) {
        this.resultFilter = s -> {
            return true;
        };
        this.classLoaders = classLoaders;
    }

    @Override // org.reflections.scanners.Scanner
    public List<Map.Entry<String, String>> scan(ClassFile classFile) {
        List<Map.Entry<String, String>> entries = new ArrayList<>();
        CtClass ctClass = null;
        try {
            try {
                ctClass = getClassPool().get(classFile.getName());
                for (CtBehavior member : ctClass.getDeclaredConstructors()) {
                    scanMember(member, entries);
                }
                for (CtBehavior member2 : ctClass.getDeclaredMethods()) {
                    scanMember(member2, entries);
                }
                if (ctClass != null) {
                    ctClass.detach();
                }
                return entries;
            } catch (Exception e) {
                throw new ReflectionsException("Could not scan method usage for " + classFile.getName(), e);
            }
        } catch (Throwable th) {
            if (ctClass != null) {
                ctClass.detach();
            }
            throw th;
        }
    }

    public Scanner filterResultsBy(Predicate<String> filter) {
        this.resultFilter = filter;
        return this;
    }

    private void scanMember(CtBehavior member, final List<Map.Entry<String, String>> entries) throws CannotCompileException {
        final String key = member.getDeclaringClass().getName() + "." + member.getMethodInfo().getName() + "(" + parameterNames(member.getMethodInfo()) + ")";
        member.instrument(new ExprEditor() { // from class: org.reflections.scanners.MemberUsageScanner.1
            @Override // javassist.expr.ExprEditor
            public void edit(NewExpr e) {
                try {
                    MemberUsageScanner.this.add(entries, e.getConstructor().getDeclaringClass().getName() + ".<init>(" + MemberUsageScanner.parameterNames(e.getConstructor().getMethodInfo()) + ")", key + " #" + e.getLineNumber());
                } catch (NotFoundException e1) {
                    throw new ReflectionsException("Could not find new instance usage in " + key, e1);
                }
            }

            @Override // javassist.expr.ExprEditor
            public void edit(MethodCall m) {
                try {
                    MemberUsageScanner.this.add(entries, m.getMethod().getDeclaringClass().getName() + "." + m.getMethodName() + "(" + MemberUsageScanner.parameterNames(m.getMethod().getMethodInfo()) + ")", key + " #" + m.getLineNumber());
                } catch (NotFoundException e) {
                    throw new ReflectionsException("Could not find member " + m.getClassName() + " in " + key, e);
                }
            }

            @Override // javassist.expr.ExprEditor
            public void edit(ConstructorCall c) {
                try {
                    MemberUsageScanner.this.add(entries, c.getConstructor().getDeclaringClass().getName() + ".<init>(" + MemberUsageScanner.parameterNames(c.getConstructor().getMethodInfo()) + ")", key + " #" + c.getLineNumber());
                } catch (NotFoundException e) {
                    throw new ReflectionsException("Could not find member " + c.getClassName() + " in " + key, e);
                }
            }

            @Override // javassist.expr.ExprEditor
            public void edit(FieldAccess f) {
                try {
                    MemberUsageScanner.this.add(entries, f.getField().getDeclaringClass().getName() + "." + f.getFieldName(), key + " #" + f.getLineNumber());
                } catch (NotFoundException e) {
                    throw new ReflectionsException("Could not find member " + f.getFieldName() + " in " + key, e);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void add(List<Map.Entry<String, String>> entries, String key, String value) {
        if (this.resultFilter.test(key)) {
            entries.add(entry(key, value));
        }
    }

    public static String parameterNames(MethodInfo info) {
        return String.join(", ", JavassistHelper.getParameters(info));
    }

    private ClassPool getClassPool() {
        if (this.classPool == null) {
            synchronized (this) {
                if (this.classPool == null) {
                    this.classPool = new ClassPool();
                    for (ClassLoader classLoader : this.classLoaders) {
                        this.classPool.appendClassPath(new LoaderClassPath(classLoader));
                    }
                }
            }
        }
        return this.classPool;
    }
}
