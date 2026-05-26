package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.network.Capes;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.class_2172;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/ReloadCommand.class */
public class ReloadCommand extends Command {
    public ReloadCommand() {
        super("reload", "Reloads many systems.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.executes(context -> {
            warning("Reloading systems, this may take a while.", new Object[0]);
            Systems.load();
            Capes.init();
            Fonts.refresh();
            MeteorExecutor.execute(() -> {
                Friends.get().forEach((v0) -> {
                    v0.updateInfo();
                });
            });
            return 1;
        });
    }
}
