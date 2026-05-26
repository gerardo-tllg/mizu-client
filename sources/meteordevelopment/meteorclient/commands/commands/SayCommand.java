package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.time.Instant;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.mixin.ClientPlayNetworkHandlerAccessor;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.starscript.Script;
import net.minecraft.class_2172;
import net.minecraft.class_2797;
import net.minecraft.class_3515;
import net.minecraft.class_7469;
import net.minecraft.class_7608;
import net.minecraft.class_7637;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/SayCommand.class */
public class SayCommand extends Command {
    public SayCommand() {
        super("say", "Sends messages in chat.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(argument("message", StringArgumentType.greedyString()).executes(context -> {
            String message;
            String msg = (String) context.getArgument("message", String.class);
            Script script = MeteorStarscript.compile(msg);
            if (script != null && (message = MeteorStarscript.run(script)) != null) {
                Instant instant = Instant.now();
                long l = class_3515.class_7426.method_43531();
                ClientPlayNetworkHandlerAccessor clientPlayNetworkHandlerAccessorMethod_1562 = mc.method_1562();
                class_7637.class_7816 lastSeenMessages = clientPlayNetworkHandlerAccessorMethod_1562.getLastSeenMessagesCollector().method_46266();
                class_7469 messageSignatureData = clientPlayNetworkHandlerAccessorMethod_1562.getMessagePacker().pack(new class_7608(message, instant, l, lastSeenMessages.comp_1073()));
                clientPlayNetworkHandlerAccessorMethod_1562.method_52787(new class_2797(message, instant, l, messageSignatureData, lastSeenMessages.comp_1074()));
                return 1;
            }
            return 1;
        }));
    }
}
