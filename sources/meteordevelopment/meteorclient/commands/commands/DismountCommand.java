package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.class_10185;
import net.minecraft.class_2172;
import net.minecraft.class_2851;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/DismountCommand.class */
public class DismountCommand extends Command {
    public DismountCommand() {
        super("dismount", "Dismounts you from entity you are riding.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.executes(context -> {
            class_10185 sneak = new class_10185(false, false, false, false, false, true, false);
            mc.method_1562().method_52787(new class_2851(sneak));
            return 1;
        });
    }
}
