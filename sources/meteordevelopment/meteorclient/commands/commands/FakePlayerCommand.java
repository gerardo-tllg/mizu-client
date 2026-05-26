package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.FakePlayerArgumentType;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.FakePlayer;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerManager;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.class_2172;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/commands/commands/FakePlayerCommand.class */
public class FakePlayerCommand extends Command {
    public FakePlayerCommand() {
        super("fake-player", "Manages fake players that you can use for testing.", new String[0]);
    }

    @Override // meteordevelopment.meteorclient.commands.Command
    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(literal("add").executes(context -> {
            FakePlayer fakePlayer = (FakePlayer) Modules.get().get(FakePlayer.class);
            FakePlayerManager.add(fakePlayer.name.get(), fakePlayer.health.get().intValue(), fakePlayer.copyInv.get().booleanValue());
            return 1;
        }).then(argument("name", StringArgumentType.word()).executes(context2 -> {
            FakePlayer fakePlayer = (FakePlayer) Modules.get().get(FakePlayer.class);
            FakePlayerManager.add(StringArgumentType.getString(context2, "name"), fakePlayer.health.get().intValue(), fakePlayer.copyInv.get().booleanValue());
            return 1;
        })));
        builder.then(literal("remove").then(argument("fp", FakePlayerArgumentType.create()).executes(context3 -> {
            FakePlayerEntity fp = FakePlayerArgumentType.get(context3);
            if (fp == null || !FakePlayerManager.contains(fp)) {
                error("Couldn't find a Fake Player with that name.", new Object[0]);
                return 1;
            }
            FakePlayerManager.remove(fp);
            info("Removed Fake Player %s.".formatted(fp.method_5477().getString()), new Object[0]);
            return 1;
        })));
        builder.then(literal("clear").executes(context4 -> {
            FakePlayerManager.clear();
            return 1;
        }));
        builder.then(literal("list").executes(context5 -> {
            info("--- Fake Players ((highlight)%s(default)) ---", Integer.valueOf(FakePlayerManager.count()));
            FakePlayerManager.forEach(fp -> {
                ChatUtils.info("(highlight)%s".formatted(fp.method_5477().getString()), new Object[0]);
            });
            return 1;
        }));
    }
}
