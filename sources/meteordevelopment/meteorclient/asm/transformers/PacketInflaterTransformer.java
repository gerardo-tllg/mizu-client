package meteordevelopment.meteorclient.asm.transformers;

import java.util.ListIterator;
import javassist.bytecode.Opcode;
import meteordevelopment.meteorclient.asm.AsmTransformer;
import meteordevelopment.meteorclient.asm.Descriptor;
import meteordevelopment.meteorclient.asm.MethodInfo;
import meteordevelopment.meteorclient.systems.modules.misc.AntiPacketKick;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/asm/transformers/PacketInflaterTransformer.class */
public class PacketInflaterTransformer extends AsmTransformer {
    private final MethodInfo decodeMethod;

    public PacketInflaterTransformer() {
        super(mapClassName("net/minecraft/class_2532"));
        this.decodeMethod = new MethodInfo("net/minecraft/class_2532", "decode", new Descriptor("Lio/netty/channel/ChannelHandlerContext;", "Lio/netty/buffer/ByteBuf;", "Ljava/util/List;", "V"), true);
    }

    @Override // meteordevelopment.meteorclient.asm.AsmTransformer
    public void transform(ClassNode klass) {
        MethodNode method = getMethod(klass, this.decodeMethod);
        if (method == null) {
            error("[Meteor Client] Could not find method PacketInflater.decode()");
        }
        int newCount = 0;
        LabelNode label = new LabelNode(new Label());
        ListIterator it = method.instructions.iterator();
        while (it.hasNext()) {
            AbstractInsnNode typeInsnNode = (AbstractInsnNode) it.next();
            if (typeInsnNode instanceof TypeInsnNode) {
                TypeInsnNode typeInsn = (TypeInsnNode) typeInsnNode;
                if (typeInsn.getOpcode() == 187 && typeInsn.desc.equals("io/netty/handler/codec/DecoderException")) {
                    newCount++;
                    if (newCount == 2) {
                        InsnList list = new InsnList();
                        list.add(new MethodInsnNode(Opcode.INVOKESTATIC, "meteordevelopment/meteorclient/systems/modules/Modules", "get", "()Lmeteordevelopment/meteorclient/systems/modules/Modules;", false));
                        list.add(new LdcInsnNode(Type.getType(AntiPacketKick.class)));
                        list.add(new MethodInsnNode(Opcode.INVOKEVIRTUAL, "meteordevelopment/meteorclient/systems/modules/Modules", "isActive", "(Ljava/lang/Class;)Z", false));
                        list.add(new JumpInsnNode(Opcode.IFNE, label));
                        method.instructions.insertBefore(typeInsnNode, list);
                    }
                }
            }
            if (newCount == 2 && typeInsnNode.getOpcode() == 191) {
                method.instructions.insert(typeInsnNode, label);
                return;
            }
        }
        error("[Meteor Client] Failed to modify PacketInflater.decode()");
    }
}
