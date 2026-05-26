package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.hunting.BaritoneElytraGoto;
import net.minecraft.class_2172;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/BaritoneElytraGotoCommand.class */
public class BaritoneElytraGotoCommand extends Command {
    public BaritoneElytraGotoCommand() {
        super("fly", "Baritone elytra flight to coordinates.", "efly", "eflyto");
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(argument("x", IntegerArgumentType.integer()).then(argument("z", IntegerArgumentType.integer()).executes(context -> {
            int x = ((Integer) context.getArgument("x", Integer.class)).intValue();
            int z = ((Integer) context.getArgument("z", Integer.class)).intValue();
            BaritoneElytraGoto module = (BaritoneElytraGoto) Modules.get().get(BaritoneElytraGoto.class);
            if (module == null) {
                error("BaritoneElytraGoto module not found.", new Object[0]);
                return 1;
            }
            module.setDestination(x, z);
            info(String.format("Flying to [%d, %d]", Integer.valueOf(x), Integer.valueOf(z)), new Object[0]);
            return 1;
        })));
        builder.then(literal("cancel").executes(context2 -> {
            BaritoneElytraGoto module = (BaritoneElytraGoto) Modules.get().get(BaritoneElytraGoto.class);
            if (module == null) {
                error("BaritoneElytraGoto module not found.", new Object[0]);
                return 1;
            }
            if (module.isActive()) {
                module.toggle();
                info("Flight cancelled.", new Object[0]);
                return 1;
            }
            warning("Module not active.", new Object[0]);
            return 1;
        }));
        builder.then(literal("status").executes(context3 -> {
            BaritoneElytraGoto module = (BaritoneElytraGoto) Modules.get().get(BaritoneElytraGoto.class);
            if (module == null) {
                error("BaritoneElytraGoto module not found.", new Object[0]);
                return 1;
            }
            if (!module.isActive()) {
                info("Module not active.", new Object[0]);
                return 1;
            }
            if (module.getDestination() == null) {
                info("Active but no destination set.", new Object[0]);
                return 1;
            }
            double distance = mc.field_1724.method_19538().method_1022(module.getDestination());
            info(String.format("Flying to [%.0f, %.0f] - %.0f blocks remaining", Double.valueOf(module.getDestination().field_1352), Double.valueOf(module.getDestination().field_1350), Double.valueOf(distance)), new Object[0]);
            return 1;
        }));
    }
}
