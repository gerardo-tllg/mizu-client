package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.hunting.BaritoneElytraGoto;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/**
 * Command for BaritoneElytraGoto module
 * 
 * Usage:
 *   .fly <x> <z> - Fly to coordinates
 *   .fly cancel - Cancel current flight
 *   .fly status - Check current destination
 */
public class BaritoneElytraGotoCommand extends Command {
    public BaritoneElytraGotoCommand() {
        super("fly", "Baritone elytra flight to coordinates.", "efly", "eflyto");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // .fly <x> <z>
        builder.then(argument("x", IntegerArgumentType.integer())
            .then(argument("z", IntegerArgumentType.integer())
                .executes(context -> {
                    int x = context.getArgument("x", Integer.class);
                    int z = context.getArgument("z", Integer.class);

                    BaritoneElytraGoto module = Modules.get().get(BaritoneElytraGoto.class);
                    if (module == null) {
                        error("BaritoneElytraGoto module not found.");
                        return SINGLE_SUCCESS;
                    }

                    module.setDestination(x, z);
                    info(String.format("Flying to [%d, %d]", x, z));

                    return SINGLE_SUCCESS;
                })
            )
        );

        // .fly cancel
        builder.then(literal("cancel").executes(context -> {
            BaritoneElytraGoto module = Modules.get().get(BaritoneElytraGoto.class);
            if (module == null) {
                error("BaritoneElytraGoto module not found.");
                return SINGLE_SUCCESS;
            }

            if (module.isActive()) {
                module.toggle();
                info("Flight cancelled.");
            } else {
                warning("Module not active.");
            }

            return SINGLE_SUCCESS;
        }));

        // .fly status
        builder.then(literal("status").executes(context -> {
            BaritoneElytraGoto module = Modules.get().get(BaritoneElytraGoto.class);
            if (module == null) {
                error("BaritoneElytraGoto module not found.");
                return SINGLE_SUCCESS;
            }

            if (!module.isActive()) {
                info("Module not active.");
                return SINGLE_SUCCESS;
            }

            if (module.getDestination() == null) {
                info("Active but no destination set.");
            } else {
                double distance = mc.player.getPos().distanceTo(module.getDestination());
                info(String.format("Flying to [%.0f, %.0f] - %.0f blocks remaining",
                    module.getDestination().x,
                    module.getDestination().z,
                    distance));
            }

            return SINGLE_SUCCESS;
        }));
    }
}
