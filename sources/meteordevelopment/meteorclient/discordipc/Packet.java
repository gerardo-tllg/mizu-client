package meteordevelopment.meteorclient.discordipc;

import com.google.gson.JsonObject;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/discordipc/Packet.class */
public final class Packet extends Record {
    private final Opcode opcode;
    private final JsonObject data;

    public Packet(Opcode opcode, JsonObject data) {
        this.opcode = opcode;
        this.data = data;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, Packet.class), Packet.class, "opcode;data", "FIELD:Lmeteordevelopment/meteorclient/discordipc/Packet;->opcode:Lmeteordevelopment/meteorclient/discordipc/Opcode;", "FIELD:Lmeteordevelopment/meteorclient/discordipc/Packet;->data:Lcom/google/gson/JsonObject;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, Packet.class), Packet.class, "opcode;data", "FIELD:Lmeteordevelopment/meteorclient/discordipc/Packet;->opcode:Lmeteordevelopment/meteorclient/discordipc/Opcode;", "FIELD:Lmeteordevelopment/meteorclient/discordipc/Packet;->data:Lcom/google/gson/JsonObject;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, Packet.class, Object.class), Packet.class, "opcode;data", "FIELD:Lmeteordevelopment/meteorclient/discordipc/Packet;->opcode:Lmeteordevelopment/meteorclient/discordipc/Opcode;", "FIELD:Lmeteordevelopment/meteorclient/discordipc/Packet;->data:Lcom/google/gson/JsonObject;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public Opcode opcode() {
        return this.opcode;
    }

    public JsonObject data() {
        return this.data;
    }
}
