package meteordevelopment.meteorclient.asm;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/asm/AsmTransformer.class */
public abstract class AsmTransformer {
    public final String targetName;

    public abstract void transform(ClassNode classNode);

    protected AsmTransformer(String targetName) {
        this.targetName = targetName;
    }

    protected MethodNode getMethod(ClassNode klass, MethodInfo methodInfo) {
        for (MethodNode method : klass.methods) {
            if (methodInfo.equals(method)) {
                return method;
            }
        }
        return null;
    }

    protected static void error(String message) {
        System.err.println(message);
        throw new RuntimeException(message);
    }

    protected static String mapClassName(String name) {
        return FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", name.replace('/', '.'));
    }
}
