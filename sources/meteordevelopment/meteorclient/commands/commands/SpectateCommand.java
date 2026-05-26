package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2172;
import net.minecraft.class_2561;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/SpectateCommand.class */
public class SpectateCommand extends Command {
    private final StaticListener shiftListener;

    public SpectateCommand() {
        super("spectate", "Allows you to spectate nearby players", new String[0]);
        this.shiftListener = new StaticListener();
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(literal("reset").executes(context -> {
            mc.method_1504(mc.field_1724);
            return 1;
        }));
        builder.then(argument("player", PlayerArgumentType.create()).executes(context2 -> {
            mc.method_1504(PlayerArgumentType.get(context2));
            mc.field_1724.method_7353(class_2561.method_43470("Sneak to un-spectate."), true);
            MeteorClient.EVENT_BUS.subscribe(this.shiftListener);
            return 1;
        }));
    }

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/SpectateCommand$StaticListener.class */
    private static class StaticListener {
        private StaticListener() {
        }

        @EventHandler
        private void onKey(KeyEvent event) {
            if (SpectateCommand.mc.field_1690.field_1832.method_1417(event.key, 0) || SpectateCommand.mc.field_1690.field_1832.method_1433(event.key)) {
                SpectateCommand.mc.method_1504(SpectateCommand.mc.field_1724);
                event.cancel();
                MeteorClient.EVENT_BUS.unsubscribe(this);
            }
        }
    }
}
