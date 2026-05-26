package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.DirectionArgumentType;
import net.minecraft.class_2172;
import net.minecraft.class_2350;
import net.minecraft.class_3532;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/RotationCommand.class */
public class RotationCommand extends Command {
    public RotationCommand() {
        super("rotation", "Modifies your rotation.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(literal("set").then(argument("direction", DirectionArgumentType.create()).executes(context -> {
            mc.field_1724.method_36457(((class_2350) context.getArgument("direction", class_2350.class)).method_62675().method_10264() * (-90));
            mc.field_1724.method_36456(((class_2350) context.getArgument("direction", class_2350.class)).method_10144());
            return 1;
        })).then(argument("pitch", FloatArgumentType.floatArg(-90.0f, 90.0f)).executes(context2 -> {
            mc.field_1724.method_36457(((Float) context2.getArgument("pitch", Float.class)).floatValue());
            return 1;
        }).then(argument("yaw", FloatArgumentType.floatArg(-180.0f, 180.0f)).executes(context3 -> {
            mc.field_1724.method_36457(((Float) context3.getArgument("pitch", Float.class)).floatValue());
            mc.field_1724.method_36456(((Float) context3.getArgument("yaw", Float.class)).floatValue());
            return 1;
        })))).then(literal("add").then(argument("pitch", FloatArgumentType.floatArg(-90.0f, 90.0f)).executes(context4 -> {
            float pitch = mc.field_1724.method_36455() + ((Float) context4.getArgument("pitch", Float.class)).floatValue();
            mc.field_1724.method_36457(pitch >= 0.0f ? Math.min(pitch, 90.0f) : Math.max(pitch, -90.0f));
            return 1;
        }).then(argument("yaw", FloatArgumentType.floatArg(-180.0f, 180.0f)).executes(context5 -> {
            float pitch = mc.field_1724.method_36455() + ((Float) context5.getArgument("pitch", Float.class)).floatValue();
            mc.field_1724.method_36457(pitch >= 0.0f ? Math.min(pitch, 90.0f) : Math.max(pitch, -90.0f));
            float yaw = mc.field_1724.method_36454() + ((Float) context5.getArgument("yaw", Float.class)).floatValue();
            mc.field_1724.method_36456(class_3532.method_15393(yaw));
            return 1;
        }))));
    }
}
