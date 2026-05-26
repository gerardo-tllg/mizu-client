package meteordevelopment.orbit.listeners;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import meteordevelopment.orbit.EventHandler;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/orbit/listeners/LambdaListener.class */
public class LambdaListener implements IListener {
    private static boolean isJava1dot8;
    private static Constructor<MethodHandles.Lookup> lookupConstructor;
    private static Method privateLookupInMethod;
    private final Class<?> target;
    private final boolean isStatic;
    private final int priority;
    private Consumer<Object> executor;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/orbit/listeners/LambdaListener$Factory.class */
    public interface Factory {
        MethodHandles.Lookup create(Method method, Class<?> cls) throws IllegalAccessException, InvocationTargetException;
    }

    public LambdaListener(Factory factory, Class<?> klass, Object object, Method method) {
        MethodHandles.Lookup lookup;
        MethodHandle methodHandle;
        MethodType invokedType;
        this.target = method.getParameters()[0].getType();
        this.isStatic = Modifier.isStatic(method.getModifiers());
        this.priority = ((EventHandler) method.getAnnotation(EventHandler.class)).priority();
        try {
            String name = method.getName();
            if (isJava1dot8) {
                boolean a = lookupConstructor.isAccessible();
                lookupConstructor.setAccessible(true);
                lookup = lookupConstructor.newInstance(klass);
                lookupConstructor.setAccessible(a);
            } else {
                lookup = factory.create(privateLookupInMethod, klass);
            }
            MethodType methodType = MethodType.methodType((Class<?>) Void.TYPE, method.getParameters()[0].getType());
            if (this.isStatic) {
                methodHandle = lookup.findStatic(klass, name, methodType);
                invokedType = MethodType.methodType(Consumer.class);
            } else {
                methodHandle = lookup.findVirtual(klass, name, methodType);
                invokedType = MethodType.methodType((Class<?>) Consumer.class, klass);
            }
            MethodHandle lambdaFactory = LambdaMetafactory.metafactory(lookup, "accept", invokedType, MethodType.methodType((Class<?>) Void.TYPE, (Class<?>) Object.class), methodHandle, methodType).getTarget();
            if (this.isStatic) {
                this.executor = (Consumer) lambdaFactory.invoke();
            } else {
                this.executor = (Consumer) lambdaFactory.invoke(object);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override // meteordevelopment.orbit.listeners.IListener
    public void call(Object event) {
        this.executor.accept(event);
    }

    @Override // meteordevelopment.orbit.listeners.IListener
    public Class<?> getTarget() {
        return this.target;
    }

    @Override // meteordevelopment.orbit.listeners.IListener
    public int getPriority() {
        return this.priority;
    }

    @Override // meteordevelopment.orbit.listeners.IListener
    public boolean isStatic() {
        return this.isStatic;
    }

    static {
        try {
            isJava1dot8 = System.getProperty("java.version").startsWith("1.8");
            if (isJava1dot8) {
                lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
            } else {
                privateLookupInMethod = MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
