package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.class_2172;
import net.minecraft.class_2828;
import net.minecraft.class_2833;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/VClipCommand.class */
public class VClipCommand extends Command {
    public VClipCommand() {
        super("vclip", "Lets you clip through blocks vertically.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(argument("blocks", DoubleArgumentType.doubleArg()).executes(context -> {
            double blocks = ((Double) context.getArgument("blocks", Double.class)).doubleValue();
            int packetsRequired = (int) Math.ceil(Math.abs(blocks / 10.0d));
            if (packetsRequired > 20) {
                packetsRequired = 1;
            }
            if (mc.field_1724.method_5765()) {
                for (int packetNumber = 0; packetNumber < packetsRequired - 1; packetNumber++) {
                    mc.field_1724.field_3944.method_52787(class_2833.method_65307(mc.field_1724.method_5854()));
                }
                mc.field_1724.method_5854().method_5814(mc.field_1724.method_5854().method_23317(), mc.field_1724.method_5854().method_23318() + blocks, mc.field_1724.method_5854().method_23321());
                mc.field_1724.field_3944.method_52787(class_2833.method_65307(mc.field_1724.method_5854()));
                return 1;
            }
            for (int packetNumber2 = 0; packetNumber2 < packetsRequired - 1; packetNumber2++) {
                mc.field_1724.field_3944.method_52787(new class_2828.class_5911(true, mc.field_1724.field_5976));
            }
            mc.field_1724.field_3944.method_52787(new class_2828.class_2829(mc.field_1724.method_23317(), mc.field_1724.method_23318() + blocks, mc.field_1724.method_23321(), true, mc.field_1724.field_5976));
            mc.field_1724.method_5814(mc.field_1724.method_23317(), mc.field_1724.method_23318() + blocks, mc.field_1724.method_23321());
            return 1;
        }));
    }
}
