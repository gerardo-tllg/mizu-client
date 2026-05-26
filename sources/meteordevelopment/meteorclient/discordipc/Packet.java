package meteordevelopment.meteorclient.discordipc;

import com.google.gson.JsonObject;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/discordipc/Packet.class */
public record Packet(Opcode opcode, JsonObject data) {
}
